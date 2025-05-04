package com.bistro.model;

import java.math.BigDecimal;

/**
 * Model class representing an item in a customer order.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private String menuItemName;
    private int quantity;
    private BigDecimal price;
    private String specialInstructions;
    
    // Default constructor
    public OrderItem() {
    }
    
    // Constructor with essential fields
    public OrderItem(int menuItemId, String menuItemName, int quantity, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Full constructor
    public OrderItem(int id, int orderId, int menuItemId, String menuItemName, 
                    int quantity, BigDecimal price, String specialInstructions) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.specialInstructions = specialInstructions;
    }
    
    // Calculate subtotal for this item
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public int getMenuItemId() {
        return menuItemId;
    }
    
    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }
    
    public String getMenuItemName() {
        return menuItemName;
    }
    
    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", menuItemId=" + menuItemId +
                ", menuItemName='" + menuItemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
