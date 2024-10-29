package com.app.trlapp.repository;


import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.trlapp.model.Customer;


public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}