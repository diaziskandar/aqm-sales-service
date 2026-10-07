package com.kencana.salesservice.dto;

public class ItemResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private ItemTypeResponse itemType; // Tambahkan properti ini

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public ItemTypeResponse getItemType() { return itemType; }
    public void setItemType(ItemTypeResponse itemType) { this.itemType = itemType; }
}
