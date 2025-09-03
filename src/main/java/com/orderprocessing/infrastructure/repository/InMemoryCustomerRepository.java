package com.orderprocessing.infrastructure.repository;

import com.orderprocessing.domain.entity.Customer;
import com.orderprocessing.domain.repository.CustomerRepository;
import com.orderprocessing.domain.valueobject.CustomerId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final Map<CustomerId, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Customer> customersByEmail = new ConcurrentHashMap<>();

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customers.put(customer.getCustomerId(), customer);
        customersByEmail.put(customer.getEmail(), customer);
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return Optional.ofNullable(customersByEmail.get(email));
    }

    @Override
    public void update(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (!customers.containsKey(customer.getCustomerId())) {
            throw new IllegalStateException("Customer not found: " + customer.getCustomerId());
        }
        
        Optional<Customer> existing = findById(customer.getCustomerId());
        existing.ifPresent(c -> {
            if (!c.getEmail().equals(customer.getEmail())) {
                customersByEmail.remove(c.getEmail());
            }
        });
        
        customers.put(customer.getCustomerId(), customer);
        customersByEmail.put(customer.getEmail(), customer);
    }

    @Override
    public boolean exists(CustomerId customerId) {
        return customers.containsKey(customerId);
    }
}