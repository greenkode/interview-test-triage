# Runtime Bugs for Developer Triage Exercise

## Overview
This e-commerce order processing system contains two intentionally introduced runtime bugs that test a developer's ability to:
1. Analyze error symptoms and stack traces
2. Navigate through a complex codebase with Domain-Driven Design patterns
3. Identify root causes through systematic debugging
4. Understand concurrency and business logic issues

## Bug #1: Inventory Race Condition 🐛

### Symptom
When running concurrent orders (Test 2), you may see errors like:
```
ERROR in concurrent order X: Failed to reserve inventory for product: PROD-003
```

### Root Cause Location
**File**: `src/main/java/com/orderprocessing/application/service/InventoryService.java`
**Lines**: 31-42 (reserve method)

### The Problem
The `checkAvailability()` method and inventory update in `reserve()` are not atomic. Between checking availability and updating inventory, another thread can reserve the same items, leading to:
- Race conditions in concurrent scenarios
- Overselling of inventory
- Failed order processing

### Key Code Issue
```java
public synchronized boolean reserve(String productId, int quantity) {
    if (!checkAvailability(productId, quantity)) {  // ← Thread A checks: OK
        return false;
    }
    
    // ← Thread B can execute checkAvailability() here and also see: OK
    
    Integer currentReserved = reserved.getOrDefault(productId, 0);
    reserved.put(productId, currentReserved + quantity);
    
    Integer currentInventory = inventory.get(productId);
    inventory.put(productId, currentInventory - quantity);  // ← Both threads update
    
    return true;
}
```

### Debugging Hints for Candidates
- Error occurs more frequently with high concurrency
- Method is marked `synchronized` but still has race conditions
- The issue is in the separation of read and write operations
- Look at how `checkAvailability()` and inventory updates interact

---

## Bug #2: Payment Processing Logic Error 🐛

### Symptom
When running Test 3 (inventory/payment edge cases), you'll see:
```
ERROR in inventory/payment test: Insufficient inventory for product: PROD-005
```

### Root Cause Location
**File**: `src/main/java/com/orderprocessing/application/service/InventoryService.java`
**Lines**: 31-42 (reserve method) 

### The Problem
In the reserve method, inventory is decremented BEFORE checking if reservation was successful. When the first large order (25 units) processes:
1. PROD-005 starts with 30 units
2. Reserve 25 units: inventory becomes 5, reserved becomes 25
3. When second order tries to reserve 10 units from remaining 5, it fails
4. **BUT** the inventory was already decremented to 5 in the first order

### Key Code Issue
```java
public synchronized boolean reserve(String productId, int quantity) {
    if (!checkAvailability(productId, quantity)) {
        return false;
    }
    
    Integer currentReserved = reserved.getOrDefault(productId, 0);
    reserved.put(productId, currentReserved + quantity);
    
    Integer currentInventory = inventory.get(productId);
    inventory.put(productId, currentInventory - quantity);  // ← Wrong order!
    
    return true;
}
```

The inventory should only be decremented AFTER confirming the reservation was successful.

### Debugging Hints for Candidates  
- Error message points to inventory, but look at the sequence of operations
- Check what happens to inventory counts after each operation
- Compare expected vs actual inventory levels
- The logic error is in the order of operations within the reserve method

---

## Additional Context for Interviewers

### What This Tests
- **Code Navigation**: Can they navigate through layered architecture (App → OrderService → InventoryService)?
- **Debugging Skills**: Do they use print statements, debugger, or analyze code flow?
- **Concurrency Understanding**: Do they understand race conditions and synchronization?
- **Business Logic**: Can they understand the domain and identify logical errors?
- **Systematic Approach**: Do they reproduce issues methodically?

### Expected Debugging Process
1. **Run the application** and observe error messages
2. **Trace the stack traces** to identify failing components  
3. **Analyze the concurrent test scenarios** to understand the context
4. **Examine the InventoryService** class where both bugs reside
5. **Identify the race condition** in checkAvailability vs reserve operations
6. **Spot the logical error** in inventory decrementing order

### Difficulty Levels
- **Junior**: May struggle with concurrency concepts, might need guidance
- **Mid-level**: Should identify both bugs with some investigation time
- **Senior**: Should quickly identify root causes and suggest proper fixes

### Proper Fixes
**Bug #1**: Make the entire check-and-reserve operation atomic
**Bug #2**: Only decrement inventory after successful reservation validation

Both bugs are in the same method but represent different types of issues - concurrency and business logic.