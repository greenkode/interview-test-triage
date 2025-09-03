package com.orderprocessing;

import com.orderprocessing.application.service.*;
import com.orderprocessing.domain.entity.*;
import com.orderprocessing.domain.valueobject.*;
import com.orderprocessing.infrastructure.repository.*;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) {
        System.out.println("Order Processing System - Testing Runtime Bugs");
        
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        
        OrderService orderService = new OrderService(
            orderRepo, customerRepo, inventoryService, paymentService
        );
        
        setupTestData(customerRepo);
        
        System.out.println("\n=== Running Test Scenarios ===");
        
        testNormalOrderFlow(orderService);
        
        testConcurrentOrderProcessing(orderService);
        
        testInventoryAndPaymentEdgeCases(orderService);
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void setupTestData(InMemoryCustomerRepository customerRepo) {
        System.out.println("Setting up test customers...");
        
        Customer customer1 = new Customer(
            new CustomerId("CUST-001"), 
            "john@example.com", 
            "John Doe"
        );
        customer1.addLoyaltyPoints(150);
        customerRepo.save(customer1);
        
        Customer customer2 = new Customer(
            new CustomerId("CUST-002"), 
            "jane@example.com", 
            "Jane Smith"
        );
        customer2.addLoyaltyPoints(600);
        customerRepo.save(customer2);
        
        Customer customer3 = new Customer(
            new CustomerId("CUST-003"), 
            "bob@example.com", 
            "Bob Wilson"
        );
        customerRepo.save(customer3);
    }
    
    private static void testNormalOrderFlow(OrderService orderService) {
        System.out.println("\n--- Test 1: Normal Order Flow ---");
        try {
            CustomerId customerId = new CustomerId("CUST-001");
            Order order = orderService.createOrder(customerId);
            
            orderService.addItemToOrder(
                order.getOrderId(), 
                "PROD-001", 
                "Widget A", 
                new BigDecimal("25.99"), 
                2
            );
            
            orderService.addItemToOrder(
                order.getOrderId(), 
                "PROD-002", 
                "Widget B", 
                new BigDecimal("15.50"), 
                3
            );
            
            orderService.processOrder(order.getOrderId(), "CREDIT_CARD");
            
            Order processedOrder = orderService.getOrder(order.getOrderId());
            System.out.println("Order processed successfully: " + processedOrder.getOrderId());
            System.out.println("Total amount: " + processedOrder.getTotalAmount());
            System.out.println("Status: " + processedOrder.getStatus());
            
        } catch (Exception e) {
            System.err.println("ERROR in normal order flow: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testConcurrentOrderProcessing(OrderService orderService) {
        System.out.println("\n--- Test 2: Concurrent Order Processing ---");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            for (int i = 0; i < 10; i++) {
                final int orderNum = i;
                executor.submit(() -> {
                    try {
                        CustomerId customerId = new CustomerId("CUST-00" + ((orderNum % 3) + 1));
                        Order order = orderService.createOrder(customerId);
                        
                        orderService.addItemToOrder(
                            order.getOrderId(),
                            "PROD-003",
                            "Widget C",
                            new BigDecimal("35.00"),
                            1
                        );
                        
                        Thread.sleep(50);
                        
                        orderService.processOrder(order.getOrderId(), "PAYPAL");
                        
                        System.out.println("Concurrent order " + orderNum + " processed: " + 
                                         order.getOrderId());
                        
                    } catch (Exception e) {
                        System.err.println("ERROR in concurrent order " + orderNum + ": " + 
                                         e.getMessage());
                    }
                });
            }
            
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("ERROR in concurrent processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testInventoryAndPaymentEdgeCases(OrderService orderService) {
        System.out.println("\n--- Test 3: Inventory and Payment Edge Cases ---");
        
        try {
            CustomerId customerId = new CustomerId("CUST-002");
            Order order1 = orderService.createOrder(customerId);
            
            orderService.addItemToOrder(
                order1.getOrderId(),
                "PROD-005",
                "Limited Widget",
                new BigDecimal("100.00"),
                25
            );
            
            orderService.processOrder(order1.getOrderId(), "DEBIT_CARD");
            System.out.println("Large quantity order processed: " + order1.getOrderId());
            
            Order order2 = orderService.createOrder(customerId);
            orderService.addItemToOrder(
                order2.getOrderId(),
                "PROD-005",
                "Limited Widget",
                new BigDecimal("100.00"),
                10
            );
            
            orderService.processOrder(order2.getOrderId(), "DEBIT_CARD");
            System.out.println("Second order processed: " + order2.getOrderId());
            
        } catch (Exception e) {
            System.err.println("ERROR in inventory/payment test: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            CustomerId customerId = new CustomerId("CUST-003");
            Order order3 = orderService.createOrder(customerId);
            
            orderService.addItemToOrder(
                order3.getOrderId(),
                "PROD-004",
                "Expensive Widget",
                new BigDecimal("999.99"),
                3
            );
            
            orderService.processOrder(order3.getOrderId(), "CREDIT_CARD");
            System.out.println("High-value order processed: " + order3.getOrderId());
            
        } catch (Exception e) {
            System.err.println("ERROR in high-value order test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}