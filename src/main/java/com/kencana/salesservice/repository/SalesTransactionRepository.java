package com.kencana.salesservice.repository;

import com.kencana.salesservice.model.SalesTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, Long> {

    // Pencarian dengan pagination untuk halaman tabel sales utama
    Page<SalesTransaction> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

    // Pencarian list biasa untuk riwayat transaksi pada modul Customer 360-degree
    List<SalesTransaction> findByCustomerNameContainingIgnoreCase(String customerName);
}