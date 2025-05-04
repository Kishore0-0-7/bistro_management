package com.bistro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.bistro.model.Order;
import com.bistro.model.User;
import com.bistro.service.OrderService;
import com.bistro.service.impl.OrderServiceImpl;
import com.bistro.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Optional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Controller for handling order-related requests.
 */
@WebServlet("/api/orders/*")
public class OrderController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    
    public OrderController() {
        this.orderService = new OrderServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated to access orders
        if (!isAuthenticated(request)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        User user = getAuthenticatedUser(request);
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all orders (admin only) or user's orders
                if (hasRole(request, "ADMIN") || hasRole(request, "STAFF")) {
                    List<Order> orders = orderService.getAllOrders();
                    logger.info("Retrieved {} orders for admin user", orders.size());
                    
                    // Log order details for debugging
                    for (Order order : orders) {
                        logger.info("Order ID: {}, totalAmount: {}, type: {}", 
                                   order.getId(), 
                                   order.getTotalAmount(), 
                                   (order.getTotalAmount() != null ? order.getTotalAmount().getClass().getName() : "null"));
                    }
                    
                    sendJsonResponse(response, orders);
                } else {
                    List<Order> userOrders = orderService.getOrdersByUserId(user.getId());
                    logger.info("Retrieved {} orders for user ID: {}", userOrders.size(), user.getId());
                    
                    // Log order details for debugging
                    for (Order order : userOrders) {
                        logger.info("Order ID: {}, totalAmount: {}, type: {}", 
                                   order.getId(), 
                                   order.getTotalAmount(), 
                                   (order.getTotalAmount() != null ? order.getTotalAmount().getClass().getName() : "null"));
                    }
                    
                    sendJsonResponse(response, userOrders);
                }
            } else if (pathInfo.equals("/recent")) {
                // Get recent orders (admin/staff only)
                if (hasRole(request, "ADMIN") || hasRole(request, "STAFF")) {
                    int limit = 10;
                    String limitParam = request.getParameter("limit");
                    if (limitParam != null && !limitParam.isEmpty()) {
                        try {
                            limit = Integer.parseInt(limitParam);
                        } catch (NumberFormatException e) {
                            // Use default limit
                        }
                    }
                    
                    List<Order> recentOrders = orderService.getRecentOrders(limit);
                    sendJsonResponse(response, recentOrders);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin or staff access required");
                }
            } else if (pathInfo.equals("/status")) {
                // Get orders by status (admin/staff only)
                if (hasRole(request, "ADMIN") || hasRole(request, "STAFF")) {
                    String status = request.getParameter("status");
                    if (status != null && !status.isEmpty()) {
                        List<Order> statusOrders = orderService.getOrdersByStatus(status);
                        sendJsonResponse(response, statusOrders);
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Status parameter is required");
                    }
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin or staff access required");
                }
            } else if (pathInfo.equals("/date-range")) {
                // Get orders by date range (admin/staff only)
                if (hasRole(request, "ADMIN") || hasRole(request, "STAFF")) {
                    String startDateStr = request.getParameter("startDate");
                    String endDateStr = request.getParameter("endDate");
                    
                    if (startDateStr != null && !startDateStr.isEmpty() && endDateStr != null && !endDateStr.isEmpty()) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date startDate = dateFormat.parse(startDateStr);
                            Date endDate = dateFormat.parse(endDateStr);
                            
                            List<Order> dateRangeOrders = orderService.getOrdersByDateRange(startDate, endDate);
                            sendJsonResponse(response, dateRangeOrders);
                        } catch (Exception e) {
                            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Use yyyy-MM-dd");
                        }
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Start date and end date parameters are required");
                    }
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Admin or staff access required");
                }
            } else {
                // Get order by ID
                try {
                    int id = Integer.parseInt(pathInfo.substring(1));
                    Optional<Order> orderOpt = orderService.getOrderById(id);
                    
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        
                        // Log order details for debugging
                        logger.info("Retrieved order ID: {}, totalAmount: {}, type: {}", 
                                  order.getId(), 
                                  order.getTotalAmount(), 
                                  (order.getTotalAmount() != null ? order.getTotalAmount().getClass().getName() : "null"));
                        
                        // Check if the user has access to this order
                        if (hasRole(request, "ADMIN") || hasRole(request, "STAFF") || order.getUserId() == user.getId()) {
                            sendJsonResponse(response, order);
                        } else {
                            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        }
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                    }
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving orders: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving orders: " + e.getMessage());
        }
    }
    
    @Override
    protected void sendJsonResponse(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (object instanceof Order) {
            Order order = (Order) object;
            
            // Create a simplified order map to ensure correct totalAmount serialization
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("userId", order.getUserId());
            orderMap.put("status", order.getStatus());
            
            // Get the order's total amount directly from the database
            java.math.BigDecimal correctAmount = null;
            int orderId = order.getId();
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT total_amount FROM orders WHERE id = ?")) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        correctAmount = rs.getBigDecimal("total_amount");
                        logger.info("Retrieved correct totalAmount from database for order ID {}: {}", orderId, correctAmount);
                    }
                }
            } catch (Exception e) {
                logger.error("Error retrieving correct totalAmount from database for order ID {}: {}", orderId, e.getMessage());
            }
            
            // Always use database value for consistent serialization
            if (correctAmount != null) {
                // Convert BigDecimal to double for consistent JSON serialization
                double amount = correctAmount.doubleValue();
                orderMap.put("totalAmount", amount);
                logger.info("Serializing order ID: {} with totalAmount: {} from database", order.getId(), amount);
            } else {
                // If database query fails, use 0 as a last resort
                orderMap.put("totalAmount", 0.0);
                logger.error("Failed to retrieve totalAmount from database for order ID: {}, using 0.0", order.getId());
            }
            
            orderMap.put("orderDate", order.getOrderDate());
            orderMap.put("deliveryDate", order.getDeliveryDate());
            orderMap.put("deliveryAddress", order.getDeliveryAddress());
            orderMap.put("paymentMethod", order.getPaymentMethod());
            orderMap.put("paymentStatus", order.getPaymentStatus());
            orderMap.put("specialInstructions", order.getSpecialInstructions());
            orderMap.put("orderItems", order.getOrderItems());
            
            // Convert to JSON and send
            String json = objectMapper.writeValueAsString(orderMap);
            logger.info("Sending order JSON: {}", json);
            response.getWriter().print(json);
        } else if (object instanceof List<?> && !((List<?>) object).isEmpty() && ((List<?>) object).get(0) instanceof Order) {
            // Handle list of orders
            List<Order> orders = (List<Order>) object;
            List<Map<String, Object>> orderMapList = new ArrayList<>();
            
            // Load all order amounts directly from the database
            Map<Integer, java.math.BigDecimal> orderAmounts = new HashMap<>();
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id, total_amount FROM orders")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        java.math.BigDecimal amount = rs.getBigDecimal("total_amount");
                        orderAmounts.put(id, amount);
                        logger.info("Retrieved totalAmount {} for order ID {} from database", amount, id);
                    }
                }
            } catch (Exception e) {
                logger.error("Error retrieving order amounts from database: {}", e.getMessage());
            }
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("userId", order.getUserId());
                orderMap.put("status", order.getStatus());
                
                // Always use database value for consistent serialization
                java.math.BigDecimal correctAmount = orderAmounts.get(order.getId());
                if (correctAmount != null) {
                    // Convert BigDecimal to double for consistent JSON serialization
                    double amount = correctAmount.doubleValue();
                    orderMap.put("totalAmount", amount);
                    logger.info("Serializing order ID: {} with totalAmount: {} from database", order.getId(), amount);
                } else {
                    // If database query fails, use 0 as a last resort
                    orderMap.put("totalAmount", 0.0);
                    logger.error("Failed to retrieve totalAmount from database for order ID: {}, using 0.0", order.getId());
                }
                
                orderMap.put("orderDate", order.getOrderDate());
                orderMap.put("deliveryDate", order.getDeliveryDate());
                orderMap.put("deliveryAddress", order.getDeliveryAddress());
                orderMap.put("paymentMethod", order.getPaymentMethod());
                orderMap.put("paymentStatus", order.getPaymentStatus());
                orderMap.put("specialInstructions", order.getSpecialInstructions());
                orderMap.put("orderItems", order.getOrderItems());
                
                orderMapList.add(orderMap);
            }
            
            // Convert to JSON and send
            String json = objectMapper.writeValueAsString(orderMapList);
            logger.info("Sending orders JSON: {}", json);
            response.getWriter().print(json);
        } else {
            // Use the default implementation for other objects
            super.sendJsonResponse(response, object);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated to place an order
        if (!isAuthenticated(request)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        User user = getAuthenticatedUser(request);
        
        try {
            String requestBody = getRequestBody(request);
            Order order = objectMapper.readValue(requestBody, Order.class);
            
            // Set the user ID
            order.setUserId(user.getId());
            
            logger.info("Placing order for user ID: {}, initial totalAmount: {}", 
                       user.getId(), order.getTotalAmount());
            
            Order placedOrder = orderService.placeOrder(order);
            
            logger.info("Order placed, ID: {}, final totalAmount: {}", 
                      placedOrder.getId(), placedOrder.getTotalAmount());
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Order placed successfully");
            responseMap.put("order", placedOrder);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            logger.error("Error placing order: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error placing order: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated to update an order
        if (!isAuthenticated(request)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Order ID is required");
            return;
        }
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            
            // Check if the order exists
            Optional<Order> existingOrderOpt = orderService.getOrderById(id);
            
            if (existingOrderOpt.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            
            Order existingOrder = existingOrderOpt.get();
            User user = getAuthenticatedUser(request);
            
            // Check if the user has access to update this order
            if (!hasRole(request, "ADMIN") && !hasRole(request, "STAFF") && existingOrder.getUserId() != user.getId()) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
            
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            // Check if this is a status update request
            if (pathInfo.endsWith("/status")) {
                if (jsonNode.has("status")) {
                    String status = jsonNode.get("status").asText();
                    
                    logger.info("Updating order status, ID: {}, new status: {}", id, status);
                    
                    boolean updated = orderService.updateOrderStatus(id, status);
                    
                    if (updated) {
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("success", true);
                        responseMap.put("message", "Order status updated successfully");
                        
                        sendJsonResponse(response, responseMap);
                    } else {
                        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to update order status");
                    }
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Status parameter is required");
                }
            } else {
                // Full order update
                Order updatedOrder = objectMapper.treeToValue(jsonNode, Order.class);
                updatedOrder.setId(id);
                
                // Preserve original user ID
                updatedOrder.setUserId(existingOrder.getUserId());
                
                logger.info("Updating order, ID: {}, totalAmount before update: {}, totalAmount after update: {}", 
                           id, existingOrder.getTotalAmount(), updatedOrder.getTotalAmount());
                
                Order result = orderService.updateOrder(updatedOrder);
                
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Order updated successfully");
                responseMap.put("order", result);
                
                sendJsonResponse(response, responseMap);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
        } catch (Exception e) {
            logger.error("Error updating order: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error updating order: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated to cancel an order
        if (!isAuthenticated(request)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Order ID is required");
            return;
        }
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            
            // Check if the order exists
            Optional<Order> existingOrderOpt = orderService.getOrderById(id);
            
            if (existingOrderOpt.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Order not found");
                return;
            }
            
            Order existingOrder = existingOrderOpt.get();
            User user = getAuthenticatedUser(request);
            
            // Check if the user has access to cancel this order
            if (!hasRole(request, "ADMIN") && existingOrder.getUserId() != user.getId()) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
            
            logger.info("Cancelling order, ID: {}, currentStatus: {}", id, existingOrder.getStatus());
            
            boolean cancelled = orderService.cancelOrder(id);
            
            if (cancelled) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Order cancelled successfully");
                
                sendJsonResponse(response, responseMap);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to cancel order");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid order ID");
        } catch (Exception e) {
            logger.error("Error cancelling order: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error cancelling order: " + e.getMessage());
        }
    }
}
