package com.orderprocessing.application.service;

import com.orderprocessing.domain.valueobject.CustomerId;
import com.orderprocessing.domain.valueobject.Money;
import com.orderprocessing.domain.valueobject.OrderId;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentService {
    private final Map<String, BigDecimal> customerBalances = new ConcurrentHashMap<>();
    private final Map<OrderId, PaymentTransaction> transactions = new ConcurrentHashMap<>();
    
    public PaymentService() {
        initializeTestBalances();
    }
    
    private void initializeTestBalances() {
        customerBalances.put("CUST-001", new BigDecimal("1000.00"));
        customerBalances.put("CUST-002", new BigDecimal("500.00"));
        customerBalances.put("CUST-003", new BigDecimal("2000.00"));
    }
    
    public void processPayment(OrderId orderId, CustomerId customerId, 
                              Money amount, String paymentMethod) {
        
        if (transactions.containsKey(orderId)) {
            throw new IllegalStateException("Payment already processed for order: " + orderId);
        }
        
        String transactionId = generateTransactionId();
        
        switch (paymentMethod) {
            case "CREDIT_CARD":
                processCreditCard(customerId, amount);
                break;
            case "DEBIT_CARD":
                processDebitCard(customerId, amount);
                break;
            case "PAYPAL":
                processPayPal(customerId, amount);
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        
        PaymentTransaction transaction = new PaymentTransaction(
            transactionId, orderId, customerId, amount, paymentMethod
        );
        transactions.put(orderId, transaction);
    }
    
    private void processCreditCard(CustomerId customerId, Money amount) {
        BigDecimal balance = customerBalances.getOrDefault(
            customerId.getValue(), 
            BigDecimal.ZERO
        );
        
        if (balance.compareTo(amount.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds for customer: " + customerId + 
                ". Available: " + balance + ", Required: " + amount.getAmount());
        }
        
        customerBalances.put(
            customerId.getValue(), 
            balance.subtract(amount.getAmount())
        );
        
        if (ThreadLocalRandom.current().nextDouble() <= 0.05) {
            throw new RuntimeException("Credit card payment failed - bank declined transaction");
        }
    }
    
    private void processDebitCard(CustomerId customerId, Money amount) {
        BigDecimal balance = customerBalances.getOrDefault(
            customerId.getValue(), 
            BigDecimal.ZERO
        );
        
        if (balance.compareTo(amount.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds for debit card payment. Customer: " + customerId + 
                ". Available: " + balance + ", Required: " + amount.getAmount());
        }
        
        customerBalances.put(
            customerId.getValue(), 
            balance.subtract(amount.getAmount())
        );
    }
    
    private void processPayPal(CustomerId customerId, Money amount) {
        if (ThreadLocalRandom.current().nextDouble() <= 0.1) {
            throw new RuntimeException("PayPal payment failed - external service unavailable");
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
    
    public PaymentTransaction getTransaction(OrderId orderId) {
        return transactions.get(orderId);
    }
    
    public static class PaymentTransaction {
        private final String transactionId;
        private final OrderId orderId;
        private final CustomerId customerId;
        private final Money amount;
        private final String paymentMethod;
        
        public PaymentTransaction(String transactionId, OrderId orderId, 
                                CustomerId customerId, Money amount, String paymentMethod) {
            this.transactionId = transactionId;
            this.orderId = orderId;
            this.customerId = customerId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public OrderId getOrderId() {
            return orderId;
        }
        
        public CustomerId getCustomerId() {
            return customerId;
        }
        
        public Money getAmount() {
            return amount;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
    }
}