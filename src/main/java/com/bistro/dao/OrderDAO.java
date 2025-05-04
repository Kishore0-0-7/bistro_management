package com.bistro.dao;

import com.bistro.model.Order;
import java.util.List;
import java.util.Date;

/**
 * Interface for Order data access operations.
 * Extends the BaseDAO with Order-specific methods.
 */
public interface OrderDAO extends BaseDAO<Order, Integer> {
    
    /**
     * Find orders by user ID.
     *
     * @param userId the user ID to search for
     * @return a list of orders for the specified user
     * @throws Exception if a database error occurs
     */
    List<Order> findByUserId(int userId) throws Exception;
    
    /**
     * Find orders by status.
     *
     * @param status the status to search for
     * @return a list of orders with the specified status
     * @throws Exception if a database error occurs
     */
    List<Order> findByStatus(String status) throws Exception;
    
    /**
     * Find orders created between the specified dates.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of orders created between the specified dates
     * @throws Exception if a database error occurs
     */
    List<Order> findByDateRange(Date startDate, Date endDate) throws Exception;
    
    /**
     * Update the status of an order.
     *
     * @param orderId the order ID
     * @param status the new status
     * @return true if the status was updated, false otherwise
     * @throws Exception if a database error occurs
     */
    boolean updateStatus(int orderId, String status) throws Exception;
    
    /**
     * Get recent orders with a limit.
     *
     * @param limit the maximum number of orders to return
     * @return a list of recent orders
     * @throws Exception if a database error occurs
     */
    List<Order> getRecentOrders(int limit) throws Exception;
}
