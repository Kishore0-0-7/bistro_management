package com.bistro.servlet;

import com.bistro.model.CartItem;
import com.bistro.model.User;
import com.bistro.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/cart-service/*")
public class CartServlet extends HttpServlet {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        
        // Debug logs
        System.out.println("CartServlet doGet - Session ID: " + session.getId());
        System.out.println("CartServlet doGet - User ID in session: " + session.getAttribute("userId"));
        
        try {
            // Ensure user ID is set before getting cart
            ensureUserIdInSession(request);
            System.out.println("CartServlet doGet - After ensureUserIdInSession, User ID: " + session.getAttribute("userId"));
            
            // Get cart ID for current session
            int cartId = CartService.getOrCreateCart(request);
            System.out.println("CartServlet doGet - Cart ID retrieved: " + cartId);
            
            // Get cart items
            List<CartItem> cartItems = CartService.getCartItems(cartId);
            System.out.println("CartServlet doGet - Cart items count: " + (cartItems != null ? cartItems.size() : "null"));
            
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    System.out.println("CartServlet doGet - Cart item: " + item.getMenuItemId() + ", " + item.getQuantity());
                }
            }
            
            // Calculate cart total
            double total = 0.0;
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    if (item.getTotal() != null) {
                        total += item.getTotal().doubleValue();
                        System.out.println("CartServlet doGet - Item total: " + item.getMenuItemId() + " = " + item.getTotal());
                    } else {
                        System.out.println("CartServlet doGet - Item " + item.getMenuItemId() + " has null total");
                    }
                }
            }
            System.out.println("CartServlet doGet - Cart total calculated: " + total);
            
            // Create response object
            Map<String, Object> result = new HashMap<>();
            result.put("items", cartItems);
            result.put("total", total);
            
            // Debug print the JSON response
            String jsonResponse = objectMapper.writeValueAsString(result);
            System.out.println("CartServlet doGet - Response JSON: " + jsonResponse);
            
            // Write response
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (SQLException e) {
            System.out.println("Error in CartServlet doGet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            objectMapper.writeValue(response.getOutputStream(), error);
        } catch (Exception e) {
            System.out.println("Unexpected error in CartServlet doGet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred: " + e.getMessage());
            objectMapper.writeValue(response.getOutputStream(), error);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        try {
            // Ensure user ID is set before adding to cart
            ensureUserIdInSession(request);
            
            // Log user authentication information
            Object userObj = request.getSession().getAttribute("user");
            Object userIdObj = request.getSession().getAttribute("userId");
            
            if (userObj != null) {
                System.out.println("User is authenticated for cart operation: " + userObj);
            } else {
                System.out.println("No user found in session for cart operation, using session only");
            }
            
            if (userIdObj != null) {
                System.out.println("Using userId for cart operation: " + userIdObj);
            }
            
            // Read request body
            CartItem item = objectMapper.readValue(request.getInputStream(), CartItem.class);
            
            // Get cart ID for current session
            int cartId = CartService.getOrCreateCart(request);
            
            // Add item to cart
            CartService.addToCart(cartId, item.getMenuItemId(), item.getQuantity());
            
            // Get updated cart items
            List<CartItem> cartItems = CartService.getCartItems(cartId);
            double total = CartService.getCartTotal(cartId);
            
            // Create response object
            Map<String, Object> result = new HashMap<>();
            result.put("items", cartItems);
            result.put("total", total);
            
            // Write response
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (SQLException e) {
            handleError(response, e);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        try {
            // Ensure user ID is set before updating cart
            ensureUserIdInSession(request);
            
            // Get path info to determine action
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null && pathInfo.equals("/sync")) {
                // Sync cart from localStorage to database
                List<CartItem> items = objectMapper.readValue(
                    request.getInputStream(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CartItem.class)
                );
                
                CartService.syncCartWithDatabase(request, items);
                
                // Get cart ID for current session
                int cartId = CartService.getOrCreateCart(request);
                
                // Get updated cart items
                List<CartItem> cartItems = CartService.getCartItems(cartId);
                double total = CartService.getCartTotal(cartId);
                
                // Create response object
                Map<String, Object> result = new HashMap<>();
                result.put("items", cartItems);
                result.put("total", total);
                
                // Write response
                objectMapper.writeValue(response.getOutputStream(), result);
                
            } else {
                // Update cart item quantity
                CartItem item = objectMapper.readValue(request.getInputStream(), CartItem.class);
                
                // Get cart ID for current session
                int cartId = CartService.getOrCreateCart(request);
                
                // Update item quantity
                CartService.updateCartItem(cartId, item.getMenuItemId(), item.getQuantity());
                
                // Get updated cart items
                List<CartItem> cartItems = CartService.getCartItems(cartId);
                double total = CartService.getCartTotal(cartId);
                
                // Create response object
                Map<String, Object> result = new HashMap<>();
                result.put("items", cartItems);
                result.put("total", total);
                
                // Write response
                objectMapper.writeValue(response.getOutputStream(), result);
            }
            
        } catch (SQLException e) {
            handleError(response, e);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
        try {
            // Ensure user ID is set before deleting from cart
            ensureUserIdInSession(request);
            
            // Get path info to determine action
            String pathInfo = request.getPathInfo();
            
            // Get cart ID for current session
            int cartId = CartService.getOrCreateCart(request);
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Clear entire cart
                CartService.clearCart(cartId);
            } else {
                // Remove specific item
                // Path should be like /api/cart/123 where 123 is the menu item ID
                try {
                    int menuItemId = Integer.parseInt(pathInfo.substring(1));
                    CartService.removeFromCart(cartId, menuItemId);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid menu item ID");
                    objectMapper.writeValue(response.getOutputStream(), error);
                    return;
                }
            }
            
            // Get updated cart items
            List<CartItem> cartItems = CartService.getCartItems(cartId);
            double total = CartService.getCartTotal(cartId);
            
            // Create response object
            Map<String, Object> result = new HashMap<>();
            result.put("items", cartItems);
            result.put("total", total);
            
            // Write response
            objectMapper.writeValue(response.getOutputStream(), result);
            
        } catch (SQLException e) {
            handleError(response, e);
        }
    }
    
    /**
     * Helper method to ensure user ID is set in session
     */
    private void ensureUserIdInSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        
        // Check if userId is already in session
        if (session.getAttribute("userId") != null) {
            System.out.println("User ID already in session: " + session.getAttribute("userId"));
            return;
        }
        
        // Check if user object is in session
        Object userObj = session.getAttribute("user");
        if (userObj != null) {
            try {
                // Try to get user ID from user object
                User user = (User) userObj;
                int userId = user.getId();
                String username = user.getUsername();
                
                // Set userId and username in session
                session.setAttribute("userId", userId);
                session.setAttribute("username", username);
                System.out.println("Set userId: " + userId + " and username: " + username + " in session from user object");
            } catch (Exception e) {
                System.out.println("Error getting userId from user object: " + e.getMessage());
                
                // Try with reflection as fallback
                try {
                    java.lang.reflect.Method getIdMethod = userObj.getClass().getMethod("getId");
                    Object result = getIdMethod.invoke(userObj);
                    if (result instanceof Integer) {
                        Integer userId = (Integer) result;
                        session.setAttribute("userId", userId);
                        System.out.println("Set userId in session using reflection: " + userId);
                    }
                    
                    java.lang.reflect.Method getUsernameMethod = userObj.getClass().getMethod("getUsername");
                    Object usernameResult = getUsernameMethod.invoke(userObj);
                    if (usernameResult instanceof String) {
                        String username = (String) usernameResult;
                        session.setAttribute("username", username);
                        System.out.println("Set username in session using reflection: " + username);
                    }
                } catch (Exception ex) {
                    System.out.println("Failed to extract user info via reflection: " + ex.getMessage());
                }
            }
        } else {
            // Alternative: Check for username directly
            if (session.getAttribute("username") != null) {
                String username = (String) session.getAttribute("username");
                System.out.println("Found username in session: " + username);
                
                // May need to lookup user ID from database here if needed
            }
        }
        
        // Log session attributes for debugging
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        System.out.println("Session attributes:");
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            System.out.println("  " + name + " = " + session.getAttribute(name));
        }
    }
    
    private void handleError(HttpServletResponse response, Exception e) throws IOException {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        objectMapper.writeValue(response.getOutputStream(), error);
    }
} 