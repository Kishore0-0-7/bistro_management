package com.bistro.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a customer order in the restaurant.
 */
public class Order {
    private int id;
    private int userId;
    private String status; // PENDING, PREPARING, READY, DELIVERED, CANCELLED
    private BigDecimal totalAmount;
    private Date orderDate;
    private Date deliveryDate;
    private String deliveryAddress;
    private String paymentMethod;
    private String paymentStatus; // PENDING, PAID, FAILED
    private String specialInstructions;
    private List<OrderItem> orderItems;
    
    // Default constructor
    public Order() {
        this.orderItems = new ArrayList<>();
        this.orderDate = new Date();
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
    }
    
    // Constructor with essential fields
    public Order(int userId, BigDecimal totalAmount, String deliveryAddress, String paymentMethod) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.orderDate = new Date();
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
        this.orderItems = new ArrayList<>();
    }
    
    // Full constructor
    public Order(int id, int userId, String status, BigDecimal totalAmount, 
                Date orderDate, Date deliveryDate, String deliveryAddress, 
                String paymentMethod, String paymentStatus, String specialInstructions) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.specialInstructions = specialInstructions;
        this.orderItems = new ArrayList<>();
    }
    
    // Method to add an item to the order
    public void addItem(OrderItem item) {
        this.orderItems.add(item);
        // Recalculate total amount
        this.calculateTotal();
    }
    
    // Method to remove an item from the order
    public void removeItem(int itemId) {
        this.orderItems.removeIf(item -> item.getMenuItemId() == itemId);
        // Recalculate total amount
        this.calculateTotal();
    }
    
    // Method to calculate the total amount of the order
    public void calculateTotal() {
        // Don't calculate if there are no items - preserve the existing amount if it's non-zero
        if (this.orderItems == null || this.orderItems.isEmpty()) {
            if (this.totalAmount == null) {
                this.totalAmount = BigDecimal.ZERO;
            }
            return;
        }
        
        // Calculate total from items
        BigDecimal calculatedTotal = this.orderItems.stream()
                .filter(item -> item != null && item.getPrice() != null) // Safe filtering for null items/prices
                .map(item -> {
                    BigDecimal price = item.getPrice();
                    int quantity = item.getQuantity();
                    return price.multiply(new BigDecimal(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Only update if we calculated a non-zero value or the current total is null/zero
        if (calculatedTotal.compareTo(BigDecimal.ZERO) > 0 || 
            this.totalAmount == null || 
            this.totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.totalAmount = calculatedTotal;
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        
        // Only recalculate total if:
        // 1. We have valid order items AND
        // 2. The current total amount is null or zero (otherwise we'd overwrite existing valid totals)
        if (orderItems != null && !orderItems.isEmpty() && 
            (this.totalAmount == null || this.totalAmount.compareTo(BigDecimal.ZERO) == 0)) {
        this.calculateTotal();
        }
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                ", items=" + orderItems.size() +
                '}';
    }
}
