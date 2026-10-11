package com.kencana.salesservice.service;

import com.kencana.salesservice.dto.ItemResponse;
import com.kencana.salesservice.dto.ItemTypeResponse;
import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.repository.SalesTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesService {

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

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

    // 3. Menyimpan Transaksi dengan Multiple Lampiran Bukti Transaksi (Opsional)
    @Transactional
    public SalesTransaction saveTransactionWithMultipleAttachments(SalesTransaction transaction, MultipartFile[] attachments) {
        if (transaction.getItems() == null || transaction.getItems().isEmpty()) {
            throw new IllegalArgumentException("Transaksi penjualan minimal harus memiliki 1 item barang.");
        }

        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }

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

                // Gabungkan path file dengan koma jika ada lebih dari 1 file
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