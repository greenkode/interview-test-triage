package com.orderprocessing.domain.event;

import com.orderprocessing.domain.valueobject.OrderId;
import com.orderprocessing.domain.valueobject.Money;

import java.time.LocalDateTime;

public class OrderProcessedEvent {
    private final OrderId orderId;
    private final LocalDateTime processedAt;
    private final Money totalAmount;

    public OrderProcessedEvent(OrderId orderId, LocalDateTime processedAt, Money totalAmount) {
        this.orderId = orderId;
        this.processedAt = processedAt;
        this.totalAmount = totalAmount;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }
}