package com.kencana.salesservice.controller;

import com.kencana.salesservice.model.Customer;
import com.kencana.salesservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // Direktori penyimpanan file scan KTP di server
    private final String UPLOAD_DIR = "uploads/ktp/";

    // 1. Endpoint mendapatkan semua customer
    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // 2. Endpoint Cek KTP / Nama Pelanggan (Untuk mengatasi 404)
   @GetMapping("/check")
    public ResponseEntity<?> checkCustomer(@RequestParam(required = false) String nik, @RequestParam(required = false) String name) {
        Optional<Customer> customer = Optional.empty();
        System.out.println("=== ENDPOINT /api/customers/check DIPANGGIL ===");
        if (nik != null && !nik.isEmpty()) {
            customer = customerRepository.findByNik(nik);
        } 
        
        if (!customer.isPresent() && name != null && !name.isEmpty()) {
            customer = customerRepository.findByNameIgnoreCase(name);
        }

        if (customer.isPresent()) {
            return ResponseEntity.ok(customer.get());
        } else {
            // Menggunakan ResponseEntity.ok() agar tidak memunculkan baris merah (404) di Network browser
            return ResponseEntity.ok().body(Map.of("exists", false, "message", "Customer belum terdaftar."));
        }
    }

   // 3. Endpoint Pendaftaran Customer Baru + Upload Banyak Lampiran Dokumen Secara Dinamis
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createCustomerWithAttachments(
            @RequestParam("name") String name,
            @RequestParam("nik") String nik,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        // Validasi apakah NIK sudah terdaftar
        if (customerRepository.findByNik(nik).isPresent()) {
            return ResponseEntity.badRequest().body("Customer dengan NIK tersebut sudah terdaftar.");
        }

        List<String> savedFilePaths = new ArrayList<>();
        if (attachments != null && attachments.length > 0) {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    try {
                        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        Path filePath = Paths.get(UPLOAD_DIR + fileName);
                        Files.write(filePath, file.getBytes());
                        savedFilePaths.add(UPLOAD_DIR + fileName);
                    } catch (IOException e) {
                        return ResponseEntity.status(500).body("Gagal mengunggah salah satu file lampiran.");
                    }
                }
            }
        }

        // Simpan data customer baru ke database
        Customer customer = new Customer();
        customer.setName(name);
        customer.setNik(nik);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setEmail(email);
        
        // Menggabungkan banyak path file dengan pemisah koma
        customer.setAttachments(savedFilePaths.isEmpty() ? null : String.join(",", savedFilePaths));

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }
}