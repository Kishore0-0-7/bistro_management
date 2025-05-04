package com.bistro.service.impl;

import com.bistro.dao.OrderDAO;
import com.bistro.dao.impl.OrderDAOImpl;
import com.bistro.model.Order;
import com.bistro.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the OrderService interface.
 */
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderDAO orderDAO;
    
    public OrderServiceImpl() {
        this.orderDAO = new OrderDAOImpl();
    }
    
    @Override
    public Order placeOrder(Order order) throws Exception {
        // Log the incoming order for debugging
        logger.info("Incoming order data - Total Amount: {}, Type: {}", 
                    order.getTotalAmount(), 
                    (order.getTotalAmount() != null ? order.getTotalAmount().getClass().getName() : "null"));
        
        // Set default values if not provided
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("PENDING");
        }
        
        if (order.getOrderDate() == null) {
            order.setOrderDate(new Date());
        }
        
        if (order.getPaymentStatus() == null || order.getPaymentStatus().isEmpty()) {
            order.setPaymentStatus("PENDING");
        }
        
        // Initialize totalAmount if null
        if (order.getTotalAmount() == null) {
            order.setTotalAmount(BigDecimal.ZERO);
            logger.info("Order total was null, initialized to zero");
        }
        
        // If order has items but no totalAmount or totalAmount is zero, calculate it
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty() && 
            (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)) {
            
            logger.info("Calculating total amount from order items");
            order.calculateTotal();
            logger.info("Calculated total amount: {}", order.getTotalAmount());
        }
        
        // Ensure totalAmount is not null before saving
        if (order.getTotalAmount() == null) {
            order.setTotalAmount(BigDecimal.ZERO);
            logger.warn("Setting default zero total amount as no valid amount could be determined");
        }
        
        logger.info("Placing new order for user ID: {} with final total amount: {}", 
                   order.getUserId(), order.getTotalAmount());
        
        // Save the order to the database
        Order savedOrder = orderDAO.save(order);
        logger.info("Order saved with ID: {}, totalAmount: {}", savedOrder.getId(), savedOrder.getTotalAmount());
        
        return savedOrder;
    }
    
    @Override
    public Order updateOrder(Order order) throws Exception {
        // Check if order exists
        Optional<Order> existingOrderOpt = orderDAO.findById(order.getId());
        
        if (existingOrderOpt.isEmpty()) {
            logger.warn("Update failed: Order with ID {} not found", order.getId());
            throw new Exception("Order not found");
        }
        
        Order existingOrder = existingOrderOpt.get();
        logger.info("Updating order ID: {}, current totalAmount: {}", 
                   order.getId(), existingOrder.getTotalAmount());
        
        // Ensure order items are preserved if not provided in the update
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            order.setOrderItems(existingOrder.getOrderItems());
            logger.info("No order items in update request, preserving existing items");
        }
        
        // Only calculate total from items if explicitly requested or if total is null/zero
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            // Try to calculate from items first
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                logger.info("Calculating total from items for update");
                order.calculateTotal();
            } 
            
            // If still zero/null, preserve the existing total
            if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                logger.info("Using existing total amount: {}", existingOrder.getTotalAmount());
                order.setTotalAmount(existingOrder.getTotalAmount());
            }
        }
        
        logger.info("Updating order with ID: {} for user ID: {}, final totalAmount: {}", 
                   order.getId(), order.getUserId(), order.getTotalAmount());
        
        return orderDAO.update(order);
    }
    
    @Override
    public boolean cancelOrder(int id) throws Exception {
        // Check if order exists
        Optional<Order> orderOpt = orderDAO.findById(id);
        
        if (orderOpt.isEmpty()) {
            logger.warn("Cancel failed: Order with ID {} not found", id);
            throw new Exception("Order not found");
        }
        
        Order order = orderOpt.get();
        
        // Check if order can be canceled
        if (!"PENDING".equals(order.getStatus())) {
            if ("CANCELLED".equals(order.getStatus())) {
                logger.warn("Order with ID {} is already cancelled", id);
                return true; // Already cancelled, consider it a success
            }
            
            if ("DELIVERED".equals(order.getStatus())) {
                logger.warn("Cannot cancel order with ID {} that has been delivered", id);
                throw new Exception("Cannot cancel an order that has been delivered");
            }
        }
        
        logger.info("Cancelling order with ID: {}", id);
        
        // Update order status to CANCELLED
        return orderDAO.updateStatus(id, "CANCELLED");
    }
    
    @Override
    public Optional<Order> getOrderById(int id) throws Exception {
        Optional<Order> orderOpt = orderDAO.findById(id);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            logger.info("Retrieved order by ID: {}, totalAmount: {}", id, order.getTotalAmount());
            
            // Ensure totalAmount is not null
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", id);
                order.setTotalAmount(BigDecimal.ZERO);
            }
        } else {
            logger.warn("Order with ID {} not found", id);
        }
        
        return orderOpt;
    }
    
    @Override
    public List<Order> getOrdersByUserId(int userId) throws Exception {
        List<Order> orders = orderDAO.findByUserId(userId);
        logger.info("Retrieved {} orders for user ID: {}", orders.size(), userId);
        
        // Ensure no order has null totalAmount
        for (Order order : orders) {
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", order.getId());
                order.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        return orders;
    }
    
    @Override
    public List<Order> getOrdersByStatus(String status) throws Exception {
        List<Order> orders = orderDAO.findByStatus(status);
        logger.info("Retrieved {} orders with status: {}", orders.size(), status);
        
        // Ensure no order has null totalAmount
        for (Order order : orders) {
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", order.getId());
                order.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        return orders;
    }
    
    @Override
    public List<Order> getOrdersByDateRange(Date startDate, Date endDate) throws Exception {
        List<Order> orders = orderDAO.findByDateRange(startDate, endDate);
        logger.info("Retrieved {} orders between {} and {}", orders.size(), startDate, endDate);
        
        // Ensure no order has null totalAmount
        for (Order order : orders) {
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", order.getId());
                order.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        return orders;
    }
    
    @Override
    public boolean updateOrderStatus(int orderId, String status) throws Exception {
        // Check if order exists
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            logger.warn("Status update failed: Order with ID {} not found", orderId);
            throw new Exception("Order not found");
        }
        
        Order existingOrder = orderOpt.get();
        BigDecimal originalAmount = existingOrder.getTotalAmount();
        logger.info("Updating status for order ID: {} from {} to: {}, original totalAmount: {}", 
                  orderId, existingOrder.getStatus(), status, originalAmount);
        
        // The DAO method now preserves the total amount internally
        boolean updated = orderDAO.updateStatus(orderId, status);
        
        if (updated) {
            // Verify the update was successful and total amount preserved
            Optional<Order> updatedOrderOpt = orderDAO.findById(orderId);
            if (updatedOrderOpt.isPresent()) {
                Order updatedOrder = updatedOrderOpt.get();
                BigDecimal updatedAmount = updatedOrder.getTotalAmount();
                
                // Only fix if the total amount was reset to zero or null
                // This respects manual changes where the amount was changed but not lost
                if ((updatedAmount == null || updatedAmount.compareTo(BigDecimal.ZERO) == 0) &&
                    (originalAmount != null && originalAmount.compareTo(BigDecimal.ZERO) > 0)) {
                    
                    logger.warn("Total amount was reset to zero after status update for order ID: {}, restoring original amount: {}", 
                              orderId, originalAmount);
                    updatedOrder.setTotalAmount(originalAmount);
                    orderDAO.update(updatedOrder);
                } else if (updatedAmount != null && updatedAmount.compareTo(BigDecimal.ZERO) > 0 && 
                          !updatedAmount.equals(originalAmount)) {
                    // If the amount changed but is not zero, it was likely manually updated
                    logger.info("Total amount changed from {} to {} for order ID: {} - likely a manual update, preserving new value",
                              originalAmount, updatedAmount, orderId);
                }
            }
        }
        
        return updated;
    }
    
    @Override
    public List<Order> getAllOrders() throws Exception {
        List<Order> orders = orderDAO.findAll();
        logger.info("Retrieved {} orders total", orders.size());
        
        // Ensure no order has null totalAmount
        for (Order order : orders) {
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", order.getId());
                order.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        return orders;
    }
    
    @Override
    public List<Order> getRecentOrders(int limit) throws Exception {
        List<Order> orders = orderDAO.getRecentOrders(limit);
        logger.info("Retrieved {} recent orders", orders.size());
        
        // Ensure no order has null totalAmount
        for (Order order : orders) {
            if (order.getTotalAmount() == null) {
                logger.warn("Order ID: {} has null totalAmount, setting to zero", order.getId());
                order.setTotalAmount(BigDecimal.ZERO);
            }
        }
        
        return orders;
    }
}
