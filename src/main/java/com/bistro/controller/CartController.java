package com.bistro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.bistro.model.MenuItem;
import com.bistro.model.OrderItem;
import com.bistro.service.MenuItemService;
import com.bistro.service.impl.MenuItemServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling shopping cart operations.
 */
@WebServlet("/api/cart/*")
public class CartController extends BaseController {
    private final MenuItemService menuItemService;
    
    public CartController() {
        this.menuItemService = new MenuItemServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        
        @SuppressWarnings("unchecked")
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
        
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("items", cart);
        responseMap.put("itemCount", cart.size());
        
        // Calculate total
        BigDecimal total = cart.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        responseMap.put("total", total);
        
        sendJsonResponse(response, responseMap);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        switch (pathInfo) {
            case "/add":
                handleAddToCart(request, response);
                break;
            case "/update":
                handleUpdateCart(request, response);
                break;
            case "/remove":
                handleRemoveFromCart(request, response);
                break;
            case "/clear":
                handleClearCart(request, response);
                break;
            default:
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                break;
        }
    }
    
    /**
     * Handle add to cart request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            int menuItemId = jsonNode.get("menuItemId").asInt();
            int quantity = jsonNode.get("quantity").asInt(1);
            String specialInstructions = jsonNode.has("specialInstructions") ? 
                    jsonNode.get("specialInstructions").asText() : null;
            
            // Get the menu item
            Optional<MenuItem> menuItemOpt = menuItemService.getMenuItemById(menuItemId);
            
            if (menuItemOpt.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Menu item not found");
                return;
            }
            
            MenuItem menuItem = menuItemOpt.get();
            
            // Check if the menu item is available
            if (!menuItem.isAvailable()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Menu item is not available");
                return;
            }
            
            // Convert double to BigDecimal using:
            menuItem.setPrice(new BigDecimal(String.valueOf(menuItem.getPrice())));
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(menuItem.getId());
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setSpecialInstructions(specialInstructions);
            
            // Get or create cart
            HttpSession session = request.getSession(true);
            
            @SuppressWarnings("unchecked")
            List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
            
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }
            
            // Check if the item is already in the cart
            boolean itemFound = false;
            
            for (OrderItem item : cart) {
                if (item.getMenuItemId() == menuItemId) {
                    // Update quantity
                    item.setQuantity(item.getQuantity() + quantity);
                    // Update special instructions if provided
                    if (specialInstructions != null) {
                        item.setSpecialInstructions(specialInstructions);
                    }
                    itemFound = true;
                    break;
                }
            }
            
            // Add new item if not found
            if (!itemFound) {
                cart.add(orderItem);
            }
            
            // Calculate total
            BigDecimal total = cart.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Item added to cart");
            responseMap.put("items", cart);
            responseMap.put("itemCount", cart.size());
            responseMap.put("total", total);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error adding item to cart: " + e.getMessage());
        }
    }
    
    /**
     * Handle update cart request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleUpdateCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            int menuItemId = jsonNode.get("menuItemId").asInt();
            int quantity = jsonNode.get("quantity").asInt();
            String specialInstructions = jsonNode.has("specialInstructions") ? 
                    jsonNode.get("specialInstructions").asText() : null;
            
            // Get cart
            HttpSession session = request.getSession(true);
            
            @SuppressWarnings("unchecked")
            List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
            
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }
            
            // Find the item in the cart
            boolean itemFound = false;
            
            for (OrderItem item : cart) {
                if (item.getMenuItemId() == menuItemId) {
                    // Update quantity
                    item.setQuantity(quantity);
                    // Update special instructions if provided
                    if (specialInstructions != null) {
                        item.setSpecialInstructions(specialInstructions);
                    }
                    itemFound = true;
                    break;
                }
            }
            
            if (!itemFound) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
                return;
            }
            
            // Remove item if quantity is 0 or less
            cart.removeIf(item -> item.getQuantity() <= 0);
            
            // Calculate total
            BigDecimal total = cart.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Cart updated");
            responseMap.put("items", cart);
            responseMap.put("itemCount", cart.size());
            responseMap.put("total", total);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error updating cart: " + e.getMessage());
        }
    }
    
    /**
     * Handle remove from cart request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            int menuItemId = jsonNode.get("menuItemId").asInt();
            
            // Get cart
            HttpSession session = request.getSession(true);
            
            @SuppressWarnings("unchecked")
            List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
            
            if (cart == null) {
                cart = new ArrayList<>();
                session.setAttribute("cart", cart);
            }
            
            // Remove the item from the cart
            boolean removed = cart.removeIf(item -> item.getMenuItemId() == menuItemId);
            
            if (!removed) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Item not found in cart");
                return;
            }
            
            // Calculate total
            BigDecimal total = cart.stream()
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Item removed from cart");
            responseMap.put("items", cart);
            responseMap.put("itemCount", cart.size());
            responseMap.put("total", total);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error removing item from cart: " + e.getMessage());
        }
    }
    
    /**
     * Handle clear cart request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleClearCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Clear cart
        HttpSession session = request.getSession(true);
        session.removeAttribute("cart");
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Cart cleared");
        responseMap.put("items", new ArrayList<>());
        responseMap.put("itemCount", 0);
        responseMap.put("total", BigDecimal.ZERO);
        
        sendJsonResponse(response, responseMap);
    }
}
