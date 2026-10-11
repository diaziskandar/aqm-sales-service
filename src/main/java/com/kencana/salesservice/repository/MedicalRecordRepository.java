package com.kencana.salesservice.repository;

import com.kencana.salesservice.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    // Mencari daftar rekam medis berdasarkan ID customer tertentu
    List<MedicalRecord> findByCustomerId(Long customerId);
    
    // Atau jika ingin mencari berdasarkan NIK customer melalui relasi
    List<MedicalRecord> findByCustomerNik(String nik);
}