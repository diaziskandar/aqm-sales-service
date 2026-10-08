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
import java.util.List;
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
    @GetMapping
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
            return ResponseEntity.status(404).body("Customer belum terdaftar.");
        }
    }

    // 3. Endpoint Pendaftaran Customer Baru + Upload Scan KTP
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createCustomerWithKtp(
            @RequestParam("name") String name,
            @RequestParam("nik") String nik,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "ktpFile", required = false) MultipartFile ktpfile) {

        // Validasi apakah NIK sudah terdaftar
        if (customerRepository.findByNik(nik).isPresent()) {
            return ResponseEntity.badRequest().body("Customer dengan NIK tersebut sudah terdaftar.");
        }

        String fileName = null;
        if (ktpfile != null && !ktpfile.isEmpty()) {
            try {
                // Buat folder uploads jika belum ada
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Generate nama unik untuk file gambar KTP
                fileName = UUID.randomUUID().toString() + "_" + ktpfile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.write(filePath, ktpfile.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Gagal mengunggah file scan KTP.");
            }
        }

        // Simpan data customer baru ke database
        Customer customer = new Customer();
        customer.setName(name);
        customer.setNik(nik);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setEmail(email);
        customer.setKtpImageUrl(fileName != null ? UPLOAD_DIR + fileName : null);

        Customer savedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(savedCustomer);
    }
}