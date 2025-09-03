package com.orderprocessing.domain.event;

import com.orderprocessing.domain.valueobject.OrderId;
import com.orderprocessing.domain.valueobject.CustomerId;

import java.time.LocalDateTime;

public class OrderCreatedEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final LocalDateTime occurredAt;

    public OrderCreatedEvent(OrderId orderId, CustomerId customerId, LocalDateTime occurredAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.occurredAt = occurredAt;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}