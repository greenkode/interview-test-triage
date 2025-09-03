package com.orderprocessing.application.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService {
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();
    private final Map<String, Integer> reserved = new ConcurrentHashMap<>();

    public InventoryService() {
        initializeInventory();
    }

    private void initializeInventory() {
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 75);
        inventory.put("PROD-004", 200);
        inventory.put("PROD-005", 30);
    }

    public boolean checkAvailability(String productId, int quantity) {
        Integer available = inventory.get(productId);
        if (available == null) {
            return false;
        }
        
        Integer reservedQty = reserved.getOrDefault(productId, 0);
        return (available - reservedQty) >= quantity;
    }

    public synchronized boolean reserve(String productId, int quantity) {
        if (!checkAvailability(productId, quantity)) {
            return false;
        }
        
        Integer currentReserved = reserved.getOrDefault(productId, 0);
        reserved.put(productId, currentReserved + quantity);
        
        Integer currentInventory = inventory.get(productId);
        inventory.put(productId, currentInventory - quantity);
        
        return true;
    }

    public synchronized void release(String productId, int quantity) {
        Integer currentReserved = reserved.get(productId);
        if (currentReserved != null && currentReserved >= quantity) {
            reserved.put(productId, currentReserved - quantity);
            
            Integer currentInventory = inventory.get(productId);
            inventory.put(productId, currentInventory + quantity);
        }
    }

    public int getAvailableQuantity(String productId) {
        Integer available = inventory.get(productId);
        if (available == null) {
            return 0;
        }
        Integer reservedQty = reserved.getOrDefault(productId, 0);
        return available - reservedQty;
    }
}