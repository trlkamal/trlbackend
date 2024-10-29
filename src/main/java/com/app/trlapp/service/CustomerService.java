package com.app.trlapp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.app.trlapp.model.Customer;
import com.app.trlapp.repository.CustomerRepository;


@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Create or update a customer
    @CacheEvict(value = "customerCache", allEntries = true)
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Retrieve a customer by ID
    @Cacheable(value = "customerCache", key = "#id")
    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    // Retrieve all customers
    @Cacheable(value = "customerCache")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @CacheEvict(value = "customerCache",key = "#id")
    public void deleteCustomer(UUID id) {
        customerRepository.deleteById(id);
    }
}