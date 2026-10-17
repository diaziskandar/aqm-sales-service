package com.kencana.salesservice.service;

import com.kencana.salesservice.client.InventoryClient; // Import Feign Client
import com.kencana.salesservice.dto.ItemResponse;
import com.kencana.salesservice.dto.ItemTypeResponse;
import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.model.SalesTransactionItem;
import com.kencana.salesservice.repository.SalesTransactionRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalesService {

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

    @Autowired
    private InventoryClient inventoryClient; // Menggunakan Feign Client untuk menghubungi inventory-service

    // 1. Mengambil data transaksi dengan dukungan Pagination, Sorting, dan Search
    public Page<SalesTransaction> getTransactions(int page, int size, String sortBy, String direction, String search, String startDate, String endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (search != null && !search.trim().isEmpty()) {
            return salesTransactionRepository.findByCustomerNameContainingIgnoreCase(search.trim(), pageable);
        }
        
        return salesTransactionRepository.findAll(pageable);
    }

    // 2. Menyimpan Transaksi Master-Detail standar (tanpa file)
    @Transactional
    public SalesTransaction saveTransaction(SalesTransaction transaction) {
        if (transaction.getItems() == null || transaction.getItems().isEmpty()) {
            throw new IllegalArgumentException("Transaksi penjualan minimal harus memiliki 1 item barang.");
        }

        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

        return salesTransactionRepository.save(transaction);
    }

    // 3. Menyimpan Transaksi dengan Multiple Lampiran Bukti Transaksi & Integrasi Stok Cabang Berdasarkan JWT
    @Transactional
    public SalesTransaction saveTransactionWithMultipleAttachments(Jwt jwt, SalesTransaction transaction, MultipartFile[] attachments) {
        if (transaction.getItems() == null || transaction.getItems().isEmpty()) {
            throw new IllegalArgumentException("Transaksi penjualan minimal harus memiliki 1 item barang.");
        }

        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

        // A. Ambil branch_id dari token JWT secara fleksibel dan aman
        Long branchId = 2L; // Default kita arahkan ke Cabang Bandung 1 (ID: 2) untuk testing jika null
        if (jwt != null) {
            // Cetak semua klaim token ke console untuk memastikan isinya di terminal
            System.out.println("=== JWT CLAIMS RECEIVED ===");
            jwt.getClaims().forEach((k, v) -> System.out.println(k + " = " + v));

            String branchIdStr = jwt.getClaimAsString("branch_id");
            
            // Jika kosong, cek barangkali tersimpan di dalam object attributes
            if (branchIdStr == null || branchIdStr.isEmpty()) {
                try {
                    Map<String, Object> attributes = jwt.getClaim("attributes");
                    if (attributes != null && attributes.get("branch_id") != null) {
                        Object val = attributes.get("branch_id");
                        branchIdStr = val instanceof List ? ((List<?>) val).get(0).toString() : val.toString();
                    }
                } catch (Exception ignored) {}
            }

            if (branchIdStr != null && !branchIdStr.isEmpty()) {
                branchId = Long.parseLong(branchIdStr);
            }
        }

        System.out.println(">>> BERHASIL MENDETEKSI BRANCH ID UNTUK TRANSAKSI INI = " + branchId);

        // B. Validasi dan kurangi stok berdasarkan cabang user melalui Feign Client (inventory-service)
        for (SalesTransactionItem item : transaction.getItems()) {
            Long itemId = item.getItemId(); 
            int soldQty = item.getQuantity();

            System.out.println(">>> MENGIRIM REQUEST DEDUCT STOK: Branch ID = " + branchId + ", Item ID = " + itemId + ", Qty = " + soldQty);

            try {
                // Memanggil inventory-service via Feign Client
                inventoryClient.deductStock(branchId, itemId, soldQty);
            } catch (FeignException.BadRequest ex) {
                // Menangkap respons error bisnis (misal: stok tidak cukup) dari inventory-service secara bersih
                String errorMsg = ex.contentUTF8();
                if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = "Stok barang tidak mencukupi di cabang ini!";
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
            } catch (FeignException ex) {
                // Menangkap error Feign lainnya
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gagal memproses pengurangan stok dari Inventory Service.");
            }
        }

        // C. Proses Upload Lampiran File
        if (attachments != null && attachments.length > 0) {
            List<String> filePaths = new ArrayList<>();
            try {
                String uploadDir = "uploads/sales/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                for (MultipartFile file : attachments) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path filePath = Paths.get(uploadDir + fileName);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        filePaths.add(uploadDir + fileName);
                    }
                }

                if (!filePaths.isEmpty()) {
                    transaction.setAttachment(String.join(", ", filePaths));
                }
            } catch (IOException e) {
                throw new RuntimeException("Gagal menyimpan file lampiran bukti transaksi", e);
            }
        }

        return salesTransactionRepository.save(transaction);
    }

    // 4. Mengambil riwayat transaksi berdasarkan nama customer
    public List<SalesTransaction> getSalesByCustomerName(String customerName) {
        return salesTransactionRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }

    // 5. Integrasi / Dummy untuk jenis item dari Inventory Service
    public List<ItemTypeResponse> getAvailableItemTypes() {
        return List.of(); 
    }

    // 6. Integrasi / Dummy untuk item dari Inventory Service
    public List<ItemResponse> getAvailableItems() {
        return List.of(); 
    }
    
    public SalesTransaction getTransactionById(Long id) {
        return salesTransactionRepository.findById(id).orElse(null);
    }
}