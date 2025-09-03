package com.orderprocessing.application.service;

import com.orderprocessing.domain.entity.Customer;
import com.orderprocessing.domain.entity.Order;
import com.orderprocessing.domain.entity.OrderItem;
import com.orderprocessing.domain.repository.CustomerRepository;
import com.orderprocessing.domain.repository.OrderRepository;
import com.orderprocessing.domain.valueobject.CustomerId;
import com.orderprocessing.domain.valueobject.Money;
import com.orderprocessing.domain.valueobject.OrderId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ConcurrentHashMap<String, ReentrantLock> orderLocks = new ConcurrentHashMap<>();

    public OrderService(OrderRepository orderRepository, 
                       CustomerRepository customerRepository,
                       InventoryService inventoryService,
                       PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }

    public Order createOrder(CustomerId customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (!customer.isPresent()) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        
        if (!customer.get().isActive()) {
            throw new IllegalStateException("Customer is not active");
        }

        Order order = new Order(customerId);
        orderRepository.save(order);
        return order;
    }

    public void addItemToOrder(OrderId orderId, String productId, String productName, 
                               BigDecimal unitPrice, int quantity) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        Order order = orderOpt.get();
        
        if (!inventoryService.checkAvailability(productId, quantity)) {
            throw new IllegalStateException("Insufficient inventory for product: " + productId);
        }

        Money price = new Money(unitPrice, "USD");
        OrderItem item = new OrderItem(productId, productName, price, quantity);
        order.addItem(item);
        
        orderRepository.update(order);
    }

    public void processOrder(OrderId orderId, String paymentMethod) {
        ReentrantLock lock = orderLocks.computeIfAbsent(
            orderId.getValue(), 
            k -> new ReentrantLock()
        );
        
        lock.lock();
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }

            Order order = orderOpt.get();
            order.setPaymentMethod(paymentMethod);
            
            reserveInventory(order);
            
            order.process();
            
            processPayment(order);
            
            applyLoyaltyPoints(order);
            
            orderRepository.update(order);
            
        } finally {
            lock.unlock();
            orderLocks.remove(orderId.getValue());
        }
    }

    private void reserveInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            boolean reserved = inventoryService.reserve(
                item.getProductId(), 
                item.getQuantity()
            );
            
            if (!reserved) {
                throw new IllegalStateException(
                    "Failed to reserve inventory for product: " + item.getProductId()
                );
            }
        }
    }

    private void processPayment(Order order) {
        Optional<Customer> customer = customerRepository.findById(order.getCustomerId());
        if (customer.isEmpty()) {
            throw new IllegalStateException("Customer not found for payment processing");
        }
        
        Money finalAmount = calculateFinalAmount(order, customer.get());
        
        try {
            paymentService.processPayment(
                order.getOrderId(),
                order.getCustomerId(),
                finalAmount,
                order.getPaymentMethod()
            );
        } catch (RuntimeException e) {
            releaseInventory(order);
            throw new IllegalStateException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    private Money calculateFinalAmount(Order order, Customer customer) {
        Money baseAmount = order.getTotalAmount();
        double discountRate = customer.getDiscountRate();
        
        if (discountRate > 0) {
            return baseAmount.multiply(1 - discountRate);
        }
        
        return baseAmount;
    }

    private void releaseInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            inventoryService.release(item.getProductId(), item.getQuantity());
        }
    }

    private void applyLoyaltyPoints(Order order) {
        Optional<Customer> customer = customerRepository.findById(order.getCustomerId());
        customer.ifPresent(c -> {
            int points = calculateLoyaltyPoints(order.getTotalAmount());
            c.addLoyaltyPoints(points);
            customerRepository.update(c);
        });
    }

    private int calculateLoyaltyPoints(Money amount) {
        return amount.getAmount().intValue() / 10;
    }

    public Order getOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public List<Order> getCustomerOrders(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }
}