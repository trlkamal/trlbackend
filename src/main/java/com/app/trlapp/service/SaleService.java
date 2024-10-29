package com.app.trlapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.app.trlapp.model.Sale;
import com.app.trlapp.model.SaledItem;
import com.app.trlapp.dto.SaleResponseDto;
import com.app.trlapp.model.Item;
import com.app.trlapp.repository.SaleRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ItemService itemService;


    @Transactional
    @CacheEvict(value = "salesCache", allEntries = true) // Evict all sales when a sale is saved
    public ResponseEntity<SaleResponseDto> saveSale(Sale sale) {
        SaleResponseDto saleResponse = new SaleResponseDto();
        boolean isSaleValid = true; // Flag to check if the sale can be processed

        // Validate stock for each SaledItem
        if (sale.getSaledItems() != null) {
            for (SaledItem saledItem : sale.getSaledItems()) {
                // Fetch the corresponding Item from the database using the item ID
                Optional<Item> itemOpt = itemService.getItemById(saledItem.getId());

                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();

                    // Check if the sale quantity is greater than the available stock
                    if (saledItem.getQuantity() > item.getStock()) {
                        // Add a message to SaleResponse if stock is insufficient
                        String errorMessage = "Item '" + item.getItemName() + "' cannot be sold more than its available stock of " + item.getStock();
                        saleResponse.addMessage(errorMessage);
                        isSaleValid = false; // Set flag to false if any item is invalid
                        continue; // Skip this item but allow other items to be processed
                    }

                    // Update the stock of the item
                    int updatedStock = item.getStock() - saledItem.getQuantity();
                    item.setStock(updatedStock);

                    // Save the updated item back to the database
                    itemService.updateItem(item.getId(), item);

                    // Clear ID of SaledItem to ensure it's treated as new in the sale
                    saledItem.setId(null);

                    // Add a success message for the item
                    saleResponse.addMessage("Item '" + item.getItemName() + "' sold successfully. Updated stock: " + updatedStock);
                } else {
                    String errorMessage = "Item with ID '" + saledItem.getId() + "' not found.";
                    saleResponse.addMessage(errorMessage);
                    isSaleValid = false; // Set flag to false if any item is invalid
                }
            }
        }

        // Proceed only if the sale is valid
        if (isSaleValid) {
            // Check if the sale is new or existing and handle accordingly
            if (sale.getId() != null && saleRepository.existsById(sale.getId())) {
                Sale existingSale = saleRepository.findById(sale.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Sale not found"));

                existingSale.setCustomerName(sale.getCustomerName());
                existingSale.setSaledItems(sale.getSaledItems());

                // Save existing sale with updated items
                saleRepository.save(existingSale);
            } else {
                // Save new sale
                saleRepository.save(sale);
            }

            saleResponse.setSale(sale); // Attach the sale details to the response

            // Return success response
            return new ResponseEntity<>(saleResponse, HttpStatus.OK);
        } else {
            saleResponse.addMessage("Sale cannot be completed due to insufficient stock for some items.");
            saleResponse.setHasError(true); // Set error flag to true if there were validation errors

            // Return error response
            return new ResponseEntity<>(saleResponse, HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }


    @Transactional
    @Cacheable(value = "salesCache", key = "#categoryName") // Cache sales by category name
    public List<Sale> getSalesByCategory(String categoryName) {
        // Fetch all sales
        List<Sale> allSales = saleRepository.findAll();
        
        // Filter sales by category name
        return allSales.stream()
            .filter(sale -> sale.getSaledItems().stream()
                .anyMatch(saledItem -> saledItem.getCategory().equalsIgnoreCase(categoryName)))
            .collect(Collectors.toList());
    }


    @Transactional
    @Cacheable(value = "salesCache", key = "#id") // Cache sale by ID
    public Optional<Sale> getSaleById(UUID id) {
        return saleRepository.findById(id);
    }

    @Transactional
    @Cacheable(value = "salesCache") // Cache all sales
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }
}
