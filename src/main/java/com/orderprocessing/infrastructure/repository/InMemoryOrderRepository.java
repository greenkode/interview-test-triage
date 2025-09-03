package com.orderprocessing.infrastructure.repository;

import com.orderprocessing.domain.entity.Order;
import com.orderprocessing.domain.repository.OrderRepository;
import com.orderprocessing.domain.valueobject.OrderId;
import com.orderprocessing.domain.valueobject.CustomerId;
import com.orderprocessing.domain.valueobject.OrderStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return orders.values().stream()
            .filter(order -> order.getCustomerId().equals(customerId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findPendingOrders() {
        return orders.values().stream()
            .filter(order -> order.getStatus() == OrderStatus.PENDING)
            .collect(Collectors.toList());
    }

    @Override
    public void update(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!orders.containsKey(order.getOrderId())) {
            throw new IllegalStateException("Order not found: " + order.getOrderId());
        }
        orders.put(order.getOrderId(), order);
    }

    @Override
    public void delete(OrderId orderId) {
        orders.remove(orderId);
    }

    @Override
    public boolean exists(OrderId orderId) {
        return orders.containsKey(orderId);
    }
}