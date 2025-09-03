package com.orderprocessing.domain.repository;

import com.orderprocessing.domain.entity.Order;
import com.orderprocessing.domain.valueobject.OrderId;
import com.orderprocessing.domain.valueobject.CustomerId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId orderId);
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findPendingOrders();
    void update(Order order);
    void delete(OrderId orderId);
    boolean exists(OrderId orderId);
}