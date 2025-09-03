package com.orderprocessing.domain.valueobject;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    SHIPPED("Shipped"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        switch (this) {
            case PENDING:
                return newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING:
                return newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED:
                return newStatus == SHIPPED;
            case SHIPPED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}