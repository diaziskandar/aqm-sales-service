package com.kencana.salesservice.client;

import com.kencana.salesservice.dto.ItemResponse;
import com.kencana.salesservice.dto.ItemTypeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// "inventory-service" adalah nama aplikasi tujuan yang terdaftar di Eureka Server
@FeignClient(name = "inventory-service")
public interface InventoryClient {

// pada interface ini berfungsi untuk melakukan koneksi maupun mengambil data dari projet inventory-service

    // Menyesuaikan dengan endpoint GET yang ada di Inventory-Service
    @GetMapping("/api/inventory/types")
    List<ItemTypeResponse> getAllItemTypes();
 
    // Tambahkan method untuk mengambil daftar item
    @GetMapping("/api/inventory")
    List<ItemResponse> getAllItems();
}