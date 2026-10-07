package com.kencana.salesservice.repository;

import com.kencana.salesservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Cek pelanggan berdasarkan Nomor KTP (NIK)
    Optional<Customer> findByNik(String nik);

    // Cek pelanggan berdasarkan Nama
    Optional<Customer> findByNameIgnoreCase(String name);
}