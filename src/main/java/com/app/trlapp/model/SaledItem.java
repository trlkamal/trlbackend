package com.app.trlapp.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class SaledItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String itemName;
    private String category;
    private double price;          // Price per unit
    private int stock;            // Available stock
    private int quantity;         // Quantity sold
    private int saledItemAmount;  // Amount for this sold item (can be calculated)

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSaledItemAmount() {
        return saledItemAmount;
    }

    public void setSaledItemAmount(int saledItemAmount) {
        this.saledItemAmount = saledItemAmount;
    }

    // Method to calculate total price for the sold item
    public double getTotalPrice() {
        return quantity * price;  // Calculate total price based on quantity sold
    }

    // Optionally add toString, hashCode, and equals methods if needed
}
