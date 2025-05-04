package com.bistro.model;

import java.math.BigDecimal;

/**
 * Represents an item in a shopping cart
 */
public class CartItem {
    private int id;
    private int cartId;
    private int menuItemId;
    private int quantity;
    private MenuItem menuItem;
    private BigDecimal total;
    
    public CartItem() {
    }
    
    public CartItem(int menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getCartId() {
        return cartId;
    }
    
    public void setCartId(int cartId) {
        this.cartId = cartId;
    }
    
    public int getMenuItemId() {
        return menuItemId;
    }
    
    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public MenuItem getMenuItem() {
        return menuItem;
    }
    
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
} 