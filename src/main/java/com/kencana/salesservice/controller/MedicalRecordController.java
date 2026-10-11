package com.kencana.salesservice.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kencana.salesservice.model.Customer;
import com.kencana.salesservice.model.MedicalRecord;
import com.kencana.salesservice.repository.CustomerRepository;
import com.kencana.salesservice.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/all")
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getRecordsByCustomer(@PathVariable Long customerId) {
        List<MedicalRecord> records = medicalRecordRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(records);
    }

    @PostMapping
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordRequest request) {
        if (request.getCustomerId() == null) {
            return ResponseEntity.badRequest().body("Customer ID tidak boleh kosong.");
        }

        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        if (!customerOpt.isPresent()) {
            return ResponseEntity.badRequest().body("Customer dengan ID tersebut tidak ditemukan.");
        }

        MedicalRecord record = new MedicalRecord();
        record.setCustomer(customerOpt.get());
        record.setExamDate(request.getExamDate() != null ? request.getExamDate() : LocalDate.now());
        record.setAudiogramData(request.getAudiogramData());
        record.setEarCondition(request.getEarCondition());
        record.setExaminerName(request.getExaminerName());

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return ResponseEntity.ok(savedRecord);
    }

    public static class MedicalRecordRequest {
        private Long customerId;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate examDate;
        
        private String audiogramData;
        private String earCondition;
        private String examinerName;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public LocalDate getExamDate() { return examDate; }
        public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

        public String getAudiogramData() { return audiogramData; }
        public void setAudiogramData(String audiogramData) { this.audiogramData = audiogramData; }

        public String getEarCondition() { return earCondition; }
        public void setEarCondition(String earCondition) { this.earCondition = earCondition; }

        public String getExaminerName() { return examinerName; }
        public void setExaminerName(String examinerName) { this.examinerName = examinerName; }
    }
}