package com.kencana.salesservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sales_transactions")
public class SalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Column(nullable = false)
    private String customerName;

    // Pastikan atribut ini ada
    private String attachment;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "transaction_id", nullable = false)
    private List<SalesTransactionItem> items;

    // Constructors
    public SalesTransaction() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // Pastikan setter & getter ini ada
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }

    public List<SalesTransactionItem> getItems() { return items; }
    public void setItems(List<SalesTransactionItem> items) { this.items = items; }
}