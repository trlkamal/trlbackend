package com.app.trlapp.controller;

import com.app.trlapp.model.Item;
import com.app.trlapp.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // Endpoint to create a new item
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        try {
            Item createdItem = itemService.saveItem(item);
            logger.info("Created new item: {}", createdItem);
            return ResponseEntity.ok(createdItem);
        } catch (Exception e) {
            logger.error("Error creating item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to retrieve all items
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        try {
            List<Item> items = itemService.getAllItems();
            logger.info("Retrieved {} items", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Error retrieving items: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to retrieve an item by its UUID
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable UUID id) {
        try {
            Optional<Item> item = itemService.getItemById(id);
            return item.map(ResponseEntity::ok)
                       .orElseGet(() -> {
                           logger.warn("Item not found for ID: {}", id);
                           return ResponseEntity.notFound().build();
                       });
        } catch (Exception e) {
            logger.error("Error retrieving item by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to update an existing item by UUID
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable UUID id, @RequestBody Item updatedItem) {
        try {
            Item item = itemService.updateItem(id, updatedItem);
            logger.info("Updated item with ID: {}", id);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            logger.error("Error updating item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to delete an item by UUID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        try {
            itemService.deleteItem(id);
            logger.info("Deleted item with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Search items by starting letter
    @GetMapping("/search")
    public List<Item> searchItems(
            @RequestParam(value = "letter", required = false) String letter,
            @RequestParam(value = "keyword", required = false) String keyword) {

        try {
            if (keyword != null && !keyword.isEmpty()) {
                List<Item> items = itemService.searchItemsByKeyword(keyword);
                logger.info("Searched items by keyword: {}", keyword);
                return items;
            }

            if (letter != null && !letter.isEmpty()) {
                List<Item> items = itemService.searchItemsByStartingLetter(letter);
                logger.info("Searched items starting with letter: {}", letter);
                return items;
            }

            List<Item> allItems = itemService.getAllItems();
            logger.info("No search criteria provided; returning all items");
            return allItems;

        } catch (Exception e) {
            logger.error("Error searching items: {}", e.getMessage(), e);
            return List.of(); // Return an empty list on error
        }
    }
    
    @GetMapping("/search/category/{category}")
    public ResponseEntity<?> findByCategory(@PathVariable String category) {
        logger.info("Request to find items by category: {}", category);

        try {
            List<Item> items = itemService.findByCategory(category);

            if (items.isEmpty()) {
                logger.warn("No items found in category: {}", category);
                return new ResponseEntity<>("No items found in the specified category.", HttpStatus.NO_CONTENT);
            }

            logger.info("Items found in category: {}", category);
            return ResponseEntity.ok(items);

        } catch (Exception ex) {
            logger.error("Error occurred while fetching items by category: {}", category, ex);
            return new ResponseEntity<>("An error occurred while processing your request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}