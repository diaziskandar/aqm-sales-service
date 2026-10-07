package com.kencana.salesservice.service;

import com.kencana.salesservice.client.InventoryClient;
import com.kencana.salesservice.dto.ItemResponse;
import com.kencana.salesservice.dto.ItemTypeResponse;
import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.repository.SalesTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SalesService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final InventoryClient inventoryClient;

    public SalesService(SalesTransactionRepository salesTransactionRepository, InventoryClient inventoryClient) {
        this.salesTransactionRepository = salesTransactionRepository;
        this.inventoryClient = inventoryClient;
    }

    public Page<SalesTransaction> getTransactions(int page, int size, String sortBy, String direction, String search, String startDateStr, String endDateStr) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        LocalDate startDate = (startDateStr != null && !startDateStr.trim().isEmpty() && !startDateStr.equals("undefined")) ? LocalDate.parse(startDateStr) : null;
        LocalDate endDate = (endDateStr != null && !endDateStr.trim().isEmpty() && !endDateStr.equals("undefined")) ? LocalDate.parse(endDateStr) : null;
        
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // Panggil query repository yang sesuai secara dinamis untuk menghindari parameter null bertipe date di PostgreSQL
        if (startDate != null && endDate != null) {
            return salesTransactionRepository.searchAndFilterByDateRange(cleanSearch, startDate, endDate, pageable);
        } else if (startDate != null) {
            return salesTransactionRepository.searchAndFilterByStartDate(cleanSearch, startDate, pageable);
        } else if (endDate != null) {
            return salesTransactionRepository.searchAndFilterByEndDate(cleanSearch, endDate, pageable);
        } else {
            return salesTransactionRepository.searchOnly(cleanSearch, pageable);
        }
    }

    // Ambil semua data transaksi sales (method lama dipertahankan)
    public List<SalesTransaction> getAllTransactions() {
        return salesTransactionRepository.findAll();
    }

    // Ambil detail transaksi berdasarkan ID
    public Optional<SalesTransaction> getTransactionById(Long id) {
        return salesTransactionRepository.findById(id);
    }

    // Simpan transaksi baru
    public SalesTransaction saveTransaction(SalesTransaction transaction) {
        return salesTransactionRepository.save(transaction);
    }

    // Hapus transaksi
    public void deleteTransaction(Long id) {
        salesTransactionRepository.deleteById(id);
    }

    // Mengambil daftar Tipe Item dari inventory-service via Feign Client
    public List<ItemTypeResponse> getAvailableItemTypes() {
        return inventoryClient.getAllItemTypes();
    }

    // Mengambil daftar Item dari inventory-service via Feign Client
    public List<ItemResponse> getAvailableItems() {
        return inventoryClient.getAllItems();
    }
}