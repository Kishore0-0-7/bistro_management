package com.bistro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.bistro.model.MenuItem;
import com.bistro.service.MenuItemService;
import com.bistro.service.impl.MenuItemServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling menu-related requests.
 */
@WebServlet("/api/menu/*")
public class MenuController extends BaseController {
    private final MenuItemService menuItemService;
    
    public MenuController() {
        this.menuItemService = new MenuItemServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all menu items
                List<MenuItem> menuItems = menuItemService.getAllMenuItems();
                sendJsonResponse(response, menuItems);
            } else if (pathInfo.equals("/featured")) {
                // Get featured menu items
                System.out.println("Fetching featured menu items");
                List<MenuItem> featuredItems = menuItemService.getFeaturedMenuItems();
                System.out.println("Found " + featuredItems.size() + " featured items");
                sendJsonResponse(response, featuredItems);
            } else if (pathInfo.equals("/categories")) {
                // Get all categories
                List<String> categories = menuItemService.getAllCategories();
                sendJsonResponse(response, categories);
            } else if (pathInfo.startsWith("/category/")) {
                // Get menu items by category
                String category = pathInfo.substring("/category/".length());
                List<MenuItem> categoryItems = menuItemService.getMenuItemsByCategory(category);
                sendJsonResponse(response, categoryItems);
            } else if (pathInfo.startsWith("/search")) {
                // Search menu items
                String query = request.getParameter("q");
                if (query != null && !query.isEmpty()) {
                    List<MenuItem> searchResults = menuItemService.searchMenuItems(query);
                    sendJsonResponse(response, searchResults);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Search query is required");
                }
            } else {
                // Get menu item by ID
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Optional<MenuItem> menuItemOpt = menuItemService.getMenuItemById(id);
                    
                    if (menuItemOpt.isPresent()) {
                        sendJsonResponse(response, menuItemOpt.get());
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Menu item not found");
                    }
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid menu item ID");
                }
            }
        } catch (Exception e) {
            System.err.println("Error in MenuController doGet: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can add menu items
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        try {
            String requestBody = getRequestBody(request);
            MenuItem menuItem = objectMapper.readValue(requestBody, MenuItem.class);
            
            MenuItem addedItem = menuItemService.addMenuItem(menuItem);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Menu item added successfully");
            responseMap.put("menuItem", addedItem);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error adding menu item: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can update menu items
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Menu item ID is required");
            return;
        }
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            
            // Check if the menu item exists
            Optional<MenuItem> existingItemOpt = menuItemService.getMenuItemById(id);
            
            if (existingItemOpt.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Menu item not found");
                return;
            }
            
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            // Check if this is a toggle availability or featured request
            if (jsonNode.has("toggleAvailability") && jsonNode.get("toggleAvailability").asBoolean()) {
                MenuItem updatedItem = menuItemService.toggleAvailability(id);
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Menu item availability toggled successfully");
                responseMap.put("menuItem", updatedItem);
                
                sendJsonResponse(response, responseMap);
                return;
            }
            
            if (jsonNode.has("toggleFeatured") && jsonNode.get("toggleFeatured").asBoolean()) {
                MenuItem updatedItem = menuItemService.toggleFeatured(id);
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Menu item featured status toggled successfully");
                responseMap.put("menuItem", updatedItem);
                
                sendJsonResponse(response, responseMap);
                return;
            }
            
            // Regular update
            MenuItem menuItem = objectMapper.readValue(requestBody, MenuItem.class);
            menuItem.setId(id); // Ensure the ID is set
            
            MenuItem updatedItem = menuItemService.updateMenuItem(menuItem);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Menu item updated successfully");
            responseMap.put("menuItem", updatedItem);
            
            sendJsonResponse(response, responseMap);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid menu item ID");
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error updating menu item: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can delete menu items
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Menu item ID is required");
            return;
        }
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            
            boolean deleted = menuItemService.deleteMenuItem(id);
            
            if (deleted) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Menu item deleted successfully");
                
                sendJsonResponse(response, responseMap);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Menu item not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid menu item ID");
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error deleting menu item: " + e.getMessage());
        }
    }
}
