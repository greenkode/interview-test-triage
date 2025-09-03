package com.orderprocessing.domain.entity;

import com.orderprocessing.domain.valueobject.OrderId;
import com.orderprocessing.domain.valueobject.CustomerId;
import com.orderprocessing.domain.valueobject.Money;
import com.orderprocessing.domain.valueobject.OrderStatus;
import com.orderprocessing.domain.event.OrderCreatedEvent;
import com.orderprocessing.domain.event.OrderProcessedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private final List<Object> domainEvents;
    private String paymentMethod;
    private boolean isPriorityOrder;

    public Order(CustomerId customerId) {
        this.orderId = new OrderId(UUID.randomUUID().toString());
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = Money.ZERO;
        this.createdAt = LocalDateTime.now();
        this.domainEvents = new ArrayList<>();
        this.isPriorityOrder = false;
        
        addDomainEvent(new OrderCreatedEvent(orderId, customerId, createdAt));
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to a non-pending order");
        }
        items.add(item);
        recalculateTotalAmount();
    }

    public void removeItem(String productId) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot remove items from a non-pending order");
        }
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }

    public void process() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending status");
        }
        
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot process an empty order");
        }

        validatePaymentMethod();
        applyDiscounts();
        
        this.status = OrderStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
        
        addDomainEvent(new OrderProcessedEvent(orderId, processedAt, totalAmount));
    }

    private void validatePaymentMethod() {
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            throw new IllegalStateException("Payment method is required");
        }
        
        if (!isValidPaymentMethod(paymentMethod)) {
            throw new IllegalArgumentException("Invalid payment method: " + paymentMethod);
        }
    }

    private boolean isValidPaymentMethod(String method) {
        return method.equals("CREDIT_CARD") || 
               method.equals("DEBIT_CARD") || 
               method.equals("PAYPAL");
    }

    private void applyDiscounts() {
        if (isPriorityOrder && totalAmount.getAmount().doubleValue() > 100) {
            totalAmount = totalAmount.multiply(0.9);
        }
    }

    public void complete() {
        if (status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Order must be in processing status to complete");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel a completed or shipped order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void addDomainEvent(Object event) {
        domainEvents.add(event);
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPriorityOrder(boolean isPriorityOrder) {
        this.isPriorityOrder = isPriorityOrder;
    }

    public boolean isPriorityOrder() {
        return isPriorityOrder;
    }
}