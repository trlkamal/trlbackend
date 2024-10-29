package com.app.trlapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.app.trlapp.model.Item;
import com.app.trlapp.repository.ItemRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // Method to save a new item
    // Method to save a new item and evict cache
    @CacheEvict(value = "itemsList", allEntries = true)
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    // Method to retrieve all items
    // Method to retrieve all items and cache the result
    @Cacheable(value = "itemsList")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // Method to retrieve a single item by its UUID
    // Method to retrieve a single item by its UUID and cache the result
    @Cacheable(value = "itemsList", key = "#id")
    public Optional<Item> getItemById(UUID id) {
        return itemRepository.findById(id);
    }

    // Method to update an item
 // Method to update an item and evict cache
    @CacheEvict(value = "itemsList", key = "#id")
    public Item updateItem(UUID id, Item updatedItem) {
        Optional<Item> existingItem = itemRepository.findById(id);

        if (existingItem.isPresent()) {
            Item item = existingItem.get();
            item.setItemName(updatedItem.getItemName());
            item.setCategory(updatedItem.getCategory());
            item.setPrice(updatedItem.getPrice());
            item.setStock(updatedItem.getStock());
            return itemRepository.save(item);
        } else {
            throw new RuntimeException("Item not found with ID: " + id);
        }
    }

    // Method to delete an item by its UUID
 // Method to delete an item by its UUID and evict cache
    @CacheEvict(value = "itemsList", key = "#id")
    public void deleteItem(UUID id) {
        itemRepository.deleteById(id);
    }
    // Method to search items that start with a specific letter
    public List<Item> searchItemsByStartingLetter(String letter) {
        return itemRepository.findByItemNameStartingWithIgnoreCase(letter);
    }

    // Method to search items that contain a keyword
    public List<Item> searchItemsByKeyword(String keyword) {
        return itemRepository.findByItemNameContainingIgnoreCase(keyword);
    }
 // Find items by category
    public List<Item> findByCategory(String category) {
        return itemRepository.findByCategoryIgnoreCase(category);
    }

}

