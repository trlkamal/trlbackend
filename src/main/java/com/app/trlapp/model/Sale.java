package com.app.trlapp.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String customerName;
    private int totalQuantity;
    private double totalAmount; // Change this to double

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id")
    private List<SaledItem> saledItems;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalAmount() { // Change getter to double
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) { // Change setter to double
        this.totalAmount = totalAmount;
    }

    public List<SaledItem> getSaledItems() {
        return saledItems;
    }

    public void setSaledItems(List<SaledItem> saledItems) {
        this.saledItems = saledItems;
    }

    public void calculateTotals() {
        this.totalQuantity = saledItems.stream().mapToInt(SaledItem::getQuantity).sum();
        this.totalAmount = saledItems.stream().mapToDouble(SaledItem::getTotalPrice).sum(); // Now this will work
    }

    // Optionally add toString, hashCode, and equals methods if needed
}
