package com.app.trlapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.app.trlapp.dto.SaleResponseDto;
import com.app.trlapp.model.Sale;
import com.app.trlapp.service.SaleService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private static final Logger logger = LoggerFactory.getLogger(SaleController.class);

    @Autowired
    private SaleService saleService;

 // Create a sale
    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody Sale sale) {
        try {
            // Return the response directly from the service
            return saleService.saveSale(sale);
        } catch (Exception e) {
            logger.error("Error creating sale: {}", e.getMessage());
            // Return 500 Internal Server Error if an unexpected exception occurs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while creating sale");
        }
    }


    // Get a sale by ID
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable UUID id) {
        try {
            Optional<Sale> sale = saleService.getSaleById(id);
            if (sale.isPresent()) {
                return ResponseEntity.ok(sale.get()); // Return 200 OK with the sale data
            } else {
                logger.warn("Sale not found for ID: {}", id);
                return ResponseEntity.notFound().build(); // Return 404 Not Found
            }
        } catch (Exception e) {
            logger.error("Error retrieving sale by ID: {}, {}", id, e.getMessage());
            return ResponseEntity.status(500).body(null); // Return 500 Internal Server Error
        }
    }

    // Get all sales
    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales() {
        try {
            List<Sale> sales = saleService.getAllSales();
            return ResponseEntity.ok(sales); // Return 200 OK with all sales data
        } catch (Exception e) {
            logger.error("Error retrieving all sales: {}", e.getMessage());
            return ResponseEntity.status(500).body(null); // Return 500 Internal Server Error
        }
    }

    // Get sales by category
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Sale>> getSalesByCategory(@PathVariable String categoryName) {
        try {
            List<Sale> sales = saleService.getSalesByCategory(categoryName);
            if (sales.isEmpty()) {
                logger.info("No sales found for category: {}", categoryName);
                return ResponseEntity.noContent().build(); // Return 204 No Content if no sales found
            } else {
                return ResponseEntity.ok(sales); // Return 200 OK with sales data
            }
        } catch (Exception e) {
            logger.error("Error retrieving sales by category: {}, {}", categoryName, e.getMessage());
            return ResponseEntity.status(500).body(null); // Return 500 Internal Server Error
        }
    }
}