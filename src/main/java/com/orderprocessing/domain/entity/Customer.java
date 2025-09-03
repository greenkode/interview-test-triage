package com.orderprocessing.domain.entity;

import com.orderprocessing.domain.valueobject.CustomerId;
import com.orderprocessing.domain.valueobject.CustomerType;

import java.time.LocalDateTime;

public class Customer {
    private final CustomerId customerId;
    private String email;
    private String name;
    private CustomerType customerType;
    private int loyaltyPoints;
    private LocalDateTime registeredAt;
    private boolean isActive;

    public Customer(CustomerId customerId, String email, String name) {
        this.customerId = customerId;
        this.email = email;
        this.name = name;
        this.customerType = CustomerType.REGULAR;
        this.loyaltyPoints = 0;
        this.registeredAt = LocalDateTime.now();
        this.isActive = true;
    }

    public void addLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        this.loyaltyPoints += points;
        updateCustomerType();
    }

    public void useLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        if (points > loyaltyPoints) {
            throw new IllegalStateException("Insufficient loyalty points");
        }
        this.loyaltyPoints -= points;
    }

    private void updateCustomerType() {
        if (loyaltyPoints >= 1000) {
            this.customerType = CustomerType.PLATINUM;
        } else if (loyaltyPoints >= 500) {
            this.customerType = CustomerType.GOLD;
        } else if (loyaltyPoints >= 100) {
            this.customerType = CustomerType.SILVER;
        }
    }

    public double getDiscountRate() {
        return customerType.getDiscountRate();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }
}