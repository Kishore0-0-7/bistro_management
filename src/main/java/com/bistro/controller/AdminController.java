package com.bistro.controller;

import com.bistro.model.User;
import com.bistro.service.MenuItemService;
import com.bistro.service.OrderService;
import com.bistro.service.UserService;
import com.bistro.service.impl.MenuItemServiceImpl;
import com.bistro.service.impl.OrderServiceImpl;
import com.bistro.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Controller for handling admin-specific operations.
 */
@WebServlet("/api/admin/*")
public class AdminController extends BaseController {
    private final UserService userService;
    private final MenuItemService menuItemService;
    private final OrderService orderService;
    
    public AdminController() {
        this.userService = new UserServiceImpl();
        this.menuItemService = new MenuItemServiceImpl();
        this.orderService = new OrderServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can access admin endpoints
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        try {
            if (pathInfo.equals("/dashboard")) {
                handleDashboard(request, response);
            } else if (pathInfo.equals("/users")) {
                handleGetUsers(request, response);
            } else if (pathInfo.matches("/users/\\d+")) {
                // Extract user ID from path
                int userId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
                handleGetUserById(request, response, userId);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Handle dashboard request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws Exception if an error occurs
     */
    private void handleDashboard(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get dashboard data
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Get all orders to calculate total revenue
        var allOrders = orderService.getAllOrders();
        
        // Calculate total revenue directly from the database
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
        try {
            // Query the database directly for order totals
            java.sql.Connection conn = com.bistro.util.DatabaseConfig.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT SUM(total_amount) FROM orders");
            java.sql.ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                java.math.BigDecimal dbTotal = rs.getBigDecimal(1);
                if (dbTotal != null) {
                    totalRevenue = dbTotal;
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            // Log total revenue for debugging
            System.out.println("Total revenue calculated from database: " + totalRevenue);
        } catch (Exception e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Count orders by status
        var pendingOrders = orderService.getOrdersByStatus("PENDING");
        var preparingOrders = orderService.getOrdersByStatus("PREPARING");
        var readyOrders = orderService.getOrdersByStatus("READY");
        var deliveredOrders = orderService.getOrdersByStatus("DELIVERED");
        var cancelledOrders = orderService.getOrdersByStatus("CANCELLED");
        
        // Create order status counts map as expected by the frontend
        Map<String, Integer> orderStatusCounts = new HashMap<>();
        orderStatusCounts.put("PENDING", pendingOrders.size());
        orderStatusCounts.put("PREPARING", preparingOrders.size());
        orderStatusCounts.put("READY", readyOrders.size());
        orderStatusCounts.put("DELIVERED", deliveredOrders.size());
        orderStatusCounts.put("CANCELLED", cancelledOrders.size());
        
        // Get recent orders with item details
        var recentOrders = orderService.getRecentOrders(5);
        
        // Format the data as expected by the frontend
        dashboardData.put("totalOrders", allOrders.size());
        dashboardData.put("totalRevenue", totalRevenue);
        dashboardData.put("pendingOrders", pendingOrders.size());
        dashboardData.put("totalUsers", userService.getAllUsers().size());
        dashboardData.put("orderStatusCounts", orderStatusCounts);
        dashboardData.put("recentOrders", recentOrders);
        
        sendJsonResponse(response, dashboardData);
    }
    
    /**
     * Handle get users request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws Exception if an error occurs
     */
    private void handleGetUsers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get all users
        sendJsonResponse(response, userService.getAllUsers());
    }
    
    /**
     * Handle get user by ID request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param userId the ID of the user to retrieve
     * @throws Exception if an error occurs
     */
    private void handleGetUserById(HttpServletRequest request, HttpServletResponse response, int userId) throws Exception {
        var userOpt = userService.getUserById(userId);
        
        if (userOpt.isPresent()) {
            sendJsonResponse(response, userOpt.get());
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can access admin endpoints
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        try {
            switch (pathInfo) {
                case "/users":
                    handleAddUser(request, response);
                    break;
                default:
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Handle add user request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws Exception if an error occurs
     */
    private void handleAddUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestBody = getRequestBody(request);
        
        // Log the request body for debugging (removing sensitive info)
        String logBody = requestBody.replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"[MASKED]\"");
        System.out.println("Admin add user request body: " + logBody);
        
        com.bistro.model.User user = objectMapper.readValue(requestBody, com.bistro.model.User.class);
        
        // Ensure we have a password before registering
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new Exception("Password is required");
        }
        
        // Log the user object (without password)
        System.out.println("Admin creating user: " + user.getUsername() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
        
        // Register the user
        com.bistro.model.User registeredUser = userService.register(user);
        
        // Return success response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "User added successfully");
        responseMap.put("user", registeredUser);
        
        sendJsonResponse(response, responseMap);
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can access admin endpoints
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        try {
            if (pathInfo.matches("/users/\\d+")) {
                // Extract user ID from path
                int userId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
                handleUpdateUser(request, response, userId);
            } else if (pathInfo.matches("/users/\\d+/password")) {
                // Extract user ID from path
                int userId = Integer.parseInt(pathInfo.substring(pathInfo.indexOf("/users/") + 7, pathInfo.lastIndexOf('/')));
                handleUpdateUserPassword(request, response, userId);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Handle update user request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param userId the ID of the user to update
     * @throws Exception if an error occurs
     */
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response, int userId) throws Exception {
        // First, check if user exists
        var userOpt = userService.getUserById(userId);
        
        if (userOpt.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }
        
        com.bistro.model.User existingUser = userOpt.get();
        
        // Read the updated user data from the request
        String requestBody = getRequestBody(request);
        com.bistro.model.User updatedUser = objectMapper.readValue(requestBody, com.bistro.model.User.class);
        
        // Set the ID to ensure we're updating the right user
        updatedUser.setId(userId);
        
        // Preserve the password hash
        updatedUser.setPasswordHash(existingUser.getPasswordHash());
        
        // Update user
        com.bistro.model.User savedUser = userService.updateProfile(updatedUser);
        
        // Send response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "User updated successfully");
        responseMap.put("user", savedUser);
        
        sendJsonResponse(response, responseMap);
    }
    
    /**
     * Handle update user password request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param userId the ID of the user to update
     * @throws Exception if an error occurs
     */
    private void handleUpdateUserPassword(HttpServletRequest request, HttpServletResponse response, int userId) throws Exception {
        // First, check if user exists
        var userOpt = userService.getUserById(userId);
        
        if (userOpt.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }
        
        com.bistro.model.User user = userOpt.get();
        
        // Read the password data from the request
        String requestBody = getRequestBody(request);
        Map<String, String> passwordData = objectMapper.readValue(requestBody, Map.class);
        
        String newPassword = passwordData.get("newPassword");
        
        if (newPassword == null || newPassword.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "New password is required");
            return;
        }
        
        // Admin can change password without old password verification
        // We use a direct database update for this
        boolean success = userService.setPassword(userId, newPassword);
        
        if (success) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Password updated successfully");
            
            sendJsonResponse(response, responseMap);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update password");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only admin can access admin endpoints
        if (!hasRole(request, "ADMIN")) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        try {
            if (pathInfo.matches("/users/\\d+")) {
                // Extract user ID from path
                int userId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf('/') + 1));
                handleDeleteUser(request, response, userId);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete user request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param userId the ID of the user to delete
     * @throws Exception if an error occurs
     */
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, int userId) throws Exception {
        // Get the current user making the request
        User currentUser = getAuthenticatedUser(request);
        
        // Prevent deleting self
        if (currentUser != null && currentUser.getId() == userId) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot delete your own account");
            return;
        }
        
        boolean deleted = userService.deleteUser(userId);
        
        if (deleted) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "User deleted successfully");
            
            sendJsonResponse(response, responseMap);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found or could not be deleted");
        }
    }

    @Override
    protected void sendJsonResponse(HttpServletResponse response, Object object) throws IOException {
        if (object instanceof Map<?, ?> && ((Map<?, ?>) object).containsKey("recentOrders")) {
            // Special processing for dashboard data with recent orders
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            Map<String, Object> dashboardData = (Map<String, Object>) object;
            List<?> recentOrders = (List<?>) dashboardData.get("recentOrders");
            
            if (recentOrders != null && !recentOrders.isEmpty() && recentOrders.get(0) instanceof com.bistro.model.Order) {
                // Load all order amounts directly from the database
                Map<Integer, java.math.BigDecimal> orderAmounts = new HashMap<>();
                try (java.sql.Connection conn = com.bistro.util.DatabaseConfig.getConnection();
                     java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT id, total_amount FROM orders")) {
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            java.math.BigDecimal amount = rs.getBigDecimal("total_amount");
                            orderAmounts.put(id, amount);
                            System.out.println("Retrieved totalAmount " + amount + " for order ID " + id + " from database for dashboard");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error retrieving order amounts from database for dashboard: " + e.getMessage());
                }
                
                // Process the recent orders to update their totalAmount
                List<Map<String, Object>> processedOrders = new ArrayList<>();
                for (Object orderObj : recentOrders) {
                    com.bistro.model.Order order = (com.bistro.model.Order) orderObj;
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", order.getId());
                    orderMap.put("userId", order.getUserId());
                    orderMap.put("status", order.getStatus());
                    
                    // Use database value if available
                    java.math.BigDecimal correctAmount = orderAmounts.get(order.getId());
                    if (correctAmount != null) {
                        // Convert BigDecimal to double for consistent JSON serialization
                        double amount = correctAmount.doubleValue();
                        orderMap.put("totalAmount", amount);
                        System.out.println("Dashboard: Using DB totalAmount for order ID: " + order.getId() + ": " + amount);
                    } else {
                        orderMap.put("totalAmount", 0.0);
                        System.out.println("Dashboard: No DB totalAmount found for order ID: " + order.getId() + ", using 0.0");
                    }
                    
                    orderMap.put("orderDate", order.getOrderDate());
                    orderMap.put("deliveryDate", order.getDeliveryDate());
                    orderMap.put("deliveryAddress", order.getDeliveryAddress());
                    orderMap.put("paymentMethod", order.getPaymentMethod());
                    orderMap.put("paymentStatus", order.getPaymentStatus());
                    orderMap.put("specialInstructions", order.getSpecialInstructions());
                    orderMap.put("orderItems", order.getOrderItems());
                    
                    processedOrders.add(orderMap);
                }
                
                // Replace the original orders with processed ones
                dashboardData.put("recentOrders", processedOrders);
            }
            
            // Convert to JSON and send
            String json = objectMapper.writeValueAsString(dashboardData);
            System.out.println("Sending dashboard JSON: " + json);
            response.getWriter().print(json);
        } else {
            // Use default implementation for other objects
            super.sendJsonResponse(response, object);
        }
    }
}
