package com.kencana.salesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Mencegah error lazy loading / infinite loop
    private Customer customer;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "audiogram_data", columnDefinition = "TEXT")
    private String audiogramData;

    @Column(name = "ear_condition", columnDefinition = "TEXT")
    private String earCondition;

    @Column(name = "examiner_name")
    private String examinerName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters tetap sama seperti sebelumnya...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public String getAudiogramData() { return audiogramData; }
    public void setAudiogramData(String audiogramData) { this.audiogramData = audiogramData; }
    public String getEarCondition() { return earCondition; }
    public void setEarCondition(String earCondition) { this.earCondition = earCondition; }
    public String getExaminerName() { return examinerName; }
    public void setExaminerName(String examinerName) { this.examinerName = examinerName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}