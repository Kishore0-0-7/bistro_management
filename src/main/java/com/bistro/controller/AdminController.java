package com.bistro.controller;

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
            switch (pathInfo) {
                case "/dashboard":
                    handleDashboard(request, response);
                    break;
                case "/users":
                    handleGetUsers(request, response);
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
        
        // No POST endpoints implemented yet
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
        
        // No PUT endpoints implemented yet
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
        
        // No DELETE endpoints implemented yet
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
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
