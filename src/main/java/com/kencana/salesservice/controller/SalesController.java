package com.kencana.salesservice.controller;

import com.kencana.salesservice.dto.ItemResponse;
import com.kencana.salesservice.dto.ItemTypeResponse;
import com.kencana.salesservice.model.SalesTransaction;
import com.kencana.salesservice.service.SalesService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping
    public ResponseEntity<Page<SalesTransaction>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String startDate, // Diubah menjadi String
            @RequestParam(required = false) String endDate   // Diubah menjadi String
    ) {
        Page<SalesTransaction> transactions = salesService.getTransactions(page, size, sortBy, direction, search, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<SalesTransaction> createTransaction(@RequestBody SalesTransaction transaction) {
        SalesTransaction savedTransaction = salesService.saveTransaction(transaction);
        return ResponseEntity.ok(savedTransaction);
    }

    @GetMapping("/inventory-types")
    public List<ItemTypeResponse> getAvailableInventoryTypes() {
        return salesService.getAvailableItemTypes();
    }

    @GetMapping("/inventory-items")
    public List<ItemResponse> getAvailableInventoryItems() {
        return salesService.getAvailableItems();
    }
}