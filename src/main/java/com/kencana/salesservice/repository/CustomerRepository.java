package com.kencana.salesservice.repository;

import com.kencana.salesservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Cek pelanggan berdasarkan Nomor KTP (NIK)
    Optional<Customer> findByNik(String nik);

    // Cek pelanggan berdasarkan Nama
    Optional<Customer> findByNameIgnoreCase(String name);

    // Tambahkan method ini untuk mendukung pencarian dengan pagination
    Page<Customer> findByNameContainingIgnoreCaseOrNikContainingIgnoreCase(String name, String nik, Pageable pageable);
}