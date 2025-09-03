package com.orderprocessing.domain.repository;

import com.orderprocessing.domain.entity.Customer;
import com.orderprocessing.domain.valueobject.CustomerId;

import java.util.Optional;

public interface CustomerRepository {
    void save(Customer customer);
    Optional<Customer> findById(CustomerId customerId);
    Optional<Customer> findByEmail(String email);
    void update(Customer customer);
    boolean exists(CustomerId customerId);
}