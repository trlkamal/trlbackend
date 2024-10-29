package com.app.trlapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.trlapp.model.Item;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

	List<Item> findByItemNameStartingWithIgnoreCase(String letter);
    // You can define custom queries here if needed

	List<Item> findByItemNameContainingIgnoreCase(String keyword);
	 List<Item> findByCategoryIgnoreCase(String category);
}