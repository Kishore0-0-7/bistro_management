package com.bistro.service;

import com.bistro.model.Order;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order-related operations.
 */
public interface OrderService {
    
    /**
     * Place a new order.
     *
     * @param order the order to place
     * @return the placed order with ID
     * @throws Exception if order placement fails
     */
    Order placeOrder(Order order) throws Exception;
    
    /**
     * Update an existing order.
     *
     * @param order the order with updated information
     * @return the updated order
     * @throws Exception if update fails
     */
    Order updateOrder(Order order) throws Exception;
    
    /**
     * Cancel an order by ID.
     *
     * @param id the order ID
     * @return true if the order was canceled, false otherwise
     * @throws Exception if cancellation fails
     */
    boolean cancelOrder(int id) throws Exception;
    
    /**
     * Get an order by ID.
     *
     * @param id the order ID
     * @return an Optional containing the order if found, or empty if not found
     * @throws Exception if retrieval fails
     */
    Optional<Order> getOrderById(int id) throws Exception;
    
    /**
     * Get orders by user ID.
     *
     * @param userId the user ID
     * @return a list of orders for the specified user
     * @throws Exception if retrieval fails
     */
    List<Order> getOrdersByUserId(int userId) throws Exception;
    
    /**
     * Get orders by status.
     *
     * @param status the status
     * @return a list of orders with the specified status
     * @throws Exception if retrieval fails
     */
    List<Order> getOrdersByStatus(String status) throws Exception;
    
    /**
     * Get orders created between the specified dates.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of orders created between the specified dates
     * @throws Exception if retrieval fails
     */
    List<Order> getOrdersByDateRange(Date startDate, Date endDate) throws Exception;
    
    /**
     * Update the status of an order.
     *
     * @param orderId the order ID
     * @param status the new status
     * @return true if the status was updated, false otherwise
     * @throws Exception if update fails
     */
    boolean updateOrderStatus(int orderId, String status) throws Exception;
    
    /**
     * Get all orders.
     *
     * @return a list of all orders
     * @throws Exception if retrieval fails
     */
    List<Order> getAllOrders() throws Exception;
    
    /**
     * Get recent orders with a limit.
     *
     * @param limit the maximum number of orders to return
     * @return a list of recent orders
     * @throws Exception if retrieval fails
     */
    List<Order> getRecentOrders(int limit) throws Exception;
}
