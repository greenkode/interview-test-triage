package com.orderprocessing.domain.entity;

import com.orderprocessing.domain.valueobject.Money;

import java.math.BigDecimal;

public class OrderItem {
    private final String productId;
    private final String productName;
    private final Money unitPrice;
    private int quantity;
    private Money subtotal;

    public OrderItem(String productId, String productName, Money unitPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        calculateSubtotal();
    }

    private void calculateSubtotal() {
        BigDecimal qty = BigDecimal.valueOf(quantity);
        this.subtotal = new Money(unitPrice.getAmount().multiply(qty), unitPrice.getCurrency());
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateSubtotal();
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getSubtotal() {
        return subtotal;
    }
}