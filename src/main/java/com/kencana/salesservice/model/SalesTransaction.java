package com.kencana.salesservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_transactions")
public class SalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    
    @Column(nullable = false)
    private String customerName;

    // Menyimpan ID referensi Item dari inventory-service
    private Long itemId;

    @Column(nullable = false)
    private String itemName;

    // Menyimpan ID referensi ItemType dari inventory-service
    private Long itemTypeId;

    @Column(nullable = false)
    private String itemType;

    private Integer quantity;

    private BigDecimal totalPrice;

    // Constructors
    public SalesTransaction() {}

    public SalesTransaction(LocalDate date, String customerName, Long itemId, String itemName, Long itemTypeId, String itemType, Integer quantity, BigDecimal totalPrice) {
        this.date = date;
        this.customerName = customerName;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemTypeId = itemTypeId;
        this.itemType = itemType;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Long getItemTypeId() { return itemTypeId; }
    public void setItemTypeId(Long itemTypeId) { this.itemTypeId = itemTypeId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}