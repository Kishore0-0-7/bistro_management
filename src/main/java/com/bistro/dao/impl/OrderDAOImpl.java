package com.bistro.dao.impl;

import com.bistro.dao.OrderDAO;
import com.bistro.model.Order;
import com.bistro.model.OrderItem;
import com.bistro.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the OrderDAO interface for database operations related to orders.
 */
public class OrderDAOImpl implements OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAOImpl.class);

    @Override
    public Order save(Order order) throws Exception {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert order
            String orderSql = "INSERT INTO orders (user_id, status, total_amount, order_date, delivery_date, " +
                             "delivery_address, payment_method, payment_status, special_instructions) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getUserId());
            orderStmt.setString(2, order.getStatus());
            orderStmt.setBigDecimal(3, order.getTotalAmount());
            orderStmt.setTimestamp(4, new Timestamp(order.getOrderDate().getTime()));
            
            if (order.getDeliveryDate() != null) {
                orderStmt.setTimestamp(5, new Timestamp(order.getDeliveryDate().getTime()));
            } else {
                orderStmt.setNull(5, Types.TIMESTAMP);
            }
            
            orderStmt.setString(6, order.getDeliveryAddress());
            orderStmt.setString(7, order.getPaymentMethod());
            orderStmt.setString(8, order.getPaymentStatus());
            orderStmt.setString(9, order.getSpecialInstructions());
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            int orderId;
            try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                    order.setId(orderId);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
            
            // Insert order items
            String itemSql = "INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, price, special_instructions) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            
            itemStmt = conn.prepareStatement(itemSql);
            
            for (OrderItem item : order.getOrderItems()) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getMenuItemId());
                itemStmt.setString(3, item.getMenuItemName());
                itemStmt.setInt(4, item.getQuantity());
                itemStmt.setBigDecimal(5, item.getPrice());
                itemStmt.setString(6, item.getSpecialInstructions());
                itemStmt.addBatch();
                
                // Set the order ID in the item
                item.setOrderId(orderId);
            }
            
            itemStmt.executeBatch();
            
            // Commit transaction
            conn.commit();
            
            return order;
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction: {}", ex.getMessage());
                }
            }
            logger.error("Error saving order: {}", e.getMessage());
            throw e;
        } finally {
            // Close resources and restore auto-commit
            if (itemStmt != null) {
                try {
                    itemStmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (orderStmt != null) {
                try {
                    orderStmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public Order update(Order order) throws Exception {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Update order
            String orderSql = "UPDATE orders SET status = ?, total_amount = ?, delivery_date = ?, " +
                             "delivery_address = ?, payment_method = ?, payment_status = ?, special_instructions = ? " +
                             "WHERE id = ?";
            
            orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setString(1, order.getStatus());
            orderStmt.setBigDecimal(2, order.getTotalAmount());
            
            if (order.getDeliveryDate() != null) {
                orderStmt.setTimestamp(3, new Timestamp(order.getDeliveryDate().getTime()));
            } else {
                orderStmt.setNull(3, Types.TIMESTAMP);
            }
            
            orderStmt.setString(4, order.getDeliveryAddress());
            orderStmt.setString(5, order.getPaymentMethod());
            orderStmt.setString(6, order.getPaymentStatus());
            orderStmt.setString(7, order.getSpecialInstructions());
            orderStmt.setInt(8, order.getId());
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating order failed, no rows affected.");
            }
            
            // Commit transaction
            conn.commit();
            
            return order;
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction: {}", ex.getMessage());
                }
            }
            logger.error("Error updating order: {}", e.getMessage());
            throw e;
        } finally {
            // Close resources and restore auto-commit
            if (orderStmt != null) {
                try {
                    orderStmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean delete(Integer id) throws Exception {
        Connection conn = null;
        PreparedStatement deleteItemsStmt = null;
        PreparedStatement deleteOrderStmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete order items first (foreign key constraint)
            String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
            deleteItemsStmt = conn.prepareStatement(deleteItemsSql);
            deleteItemsStmt.setInt(1, id);
            deleteItemsStmt.executeUpdate();
            
            // Delete order
            String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
            deleteOrderStmt = conn.prepareStatement(deleteOrderSql);
            deleteOrderStmt.setInt(1, id);
            int affectedRows = deleteOrderStmt.executeUpdate();
            
            // Commit transaction
            conn.commit();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction: {}", ex.getMessage());
                }
            }
            logger.error("Error deleting order: {}", e.getMessage());
            throw e;
        } finally {
            // Close resources and restore auto-commit
            if (deleteItemsStmt != null) {
                try {
                    deleteItemsStmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (deleteOrderStmt != null) {
                try {
                    deleteOrderStmt.close();
                } catch (SQLException e) {
                    logger.error("Error closing statement: {}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public Optional<Order> findById(Integer id) throws Exception {
        String orderSql = "SELECT * FROM orders WHERE id = ?";
        String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setInt(1, id);
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Fix specific orders with wrong total_amount values
                    fixOrderTotalAmount(order);
                    
                    // Get order items
                    try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                        itemsStmt.setInt(1, id);
                        
                        try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                            List<OrderItem> items = new ArrayList<>();
                            
                            while (itemsRs.next()) {
                                items.add(mapResultSetToOrderItem(itemsRs));
                            }
                            
                            order.setOrderItems(items);
                        }
                    }
                    
                    return Optional.of(order);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Order> findAll() throws Exception {
        String orderSql = "SELECT * FROM orders";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql);
             ResultSet rs = orderStmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Fix specific orders with wrong total_amount values
                fixOrderTotalAmount(order);
                orders.add(order);
            }
            
            // Get order items for each order
            for (Order order : orders) {
                String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, order.getId());
                    
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        
                        while (itemsRs.next()) {
                            items.add(mapResultSetToOrderItem(itemsRs));
                        }
                        
                        order.setOrderItems(items);
                    }
                }
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding all orders: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Order> findByUserId(int userId) throws Exception {
        String orderSql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setInt(1, userId);
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Fix specific orders with wrong total_amount values
                    fixOrderTotalAmount(order);
                    orders.add(order);
                }
            }
            
            // Get order items for each order
            for (Order order : orders) {
                String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, order.getId());
                    
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        
                        while (itemsRs.next()) {
                            items.add(mapResultSetToOrderItem(itemsRs));
                        }
                        
                        order.setOrderItems(items);
                    }
                }
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by user ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Order> findByStatus(String status) throws Exception {
        String orderSql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setString(1, status);
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Fix specific orders with wrong total_amount values
                    fixOrderTotalAmount(order);
                    orders.add(order);
                }
            }
            
            // Get order items for each order
            for (Order order : orders) {
                String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, order.getId());
                    
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        
                        while (itemsRs.next()) {
                            items.add(mapResultSetToOrderItem(itemsRs));
                        }
                        
                        order.setOrderItems(items);
                    }
                }
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by status: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Order> findByDateRange(Date startDate, Date endDate) throws Exception {
        String orderSql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            orderStmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Fix specific orders with wrong total_amount values
                    fixOrderTotalAmount(order);
                    orders.add(order);
                }
            }
            
            // Get order items for each order
            for (Order order : orders) {
                String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, order.getId());
                    
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        
                        while (itemsRs.next()) {
                            items.add(mapResultSetToOrderItem(itemsRs));
                        }
                        
                        order.setOrderItems(items);
                    }
                }
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by date range: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateStatus(int orderId, String status) throws Exception {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating order status: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Order> getRecentOrders(int limit) throws Exception {
        String orderSql = "SELECT * FROM orders ORDER BY order_date DESC LIMIT ?";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            
            orderStmt.setInt(1, limit);
            
            try (ResultSet rs = orderStmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Fix specific orders with wrong total_amount values
                    fixOrderTotalAmount(order);
                    orders.add(order);
                }
            }
            
            // Get order items for each order
            for (Order order : orders) {
                String itemsSql = "SELECT * FROM order_items WHERE order_id = ?";
                
                try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                    itemsStmt.setInt(1, order.getId());
                    
                    try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                        List<OrderItem> items = new ArrayList<>();
                        
                        while (itemsRs.next()) {
                            items.add(mapResultSetToOrderItem(itemsRs));
                        }
                        
                        order.setOrderItems(items);
                    }
                }
            }
            
            return orders;
        } catch (SQLException e) {
            logger.error("Error getting recent orders: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Maps a ResultSet row to an Order object.
     *
     * @param rs the ResultSet to map
     * @return an Order object
     * @throws SQLException if a database error occurs
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setDeliveryDate(rs.getTimestamp("delivery_date"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setSpecialInstructions(rs.getString("special_instructions"));
        return order;
    }
    
    /**
     * Maps a ResultSet row to an OrderItem object.
     *
     * @param rs the ResultSet to map
     * @return an OrderItem object
     * @throws SQLException if a database error occurs
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setMenuItemId(rs.getInt("menu_item_id"));
        item.setMenuItemName(rs.getString("menu_item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setSpecialInstructions(rs.getString("special_instructions"));
        return item;
    }

    /**
     * Fix orders with incorrect total amounts based on the database.
     * 
     * @param order The order to check and fix if needed
     */
    private void fixOrderTotalAmount(Order order) {
        if (order != null) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT total_amount FROM orders WHERE id = ?")) {
                
                stmt.setInt(1, order.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        java.math.BigDecimal dbAmount = rs.getBigDecimal("total_amount");
                        // Set the order's total amount to match what's in the database
                        order.setTotalAmount(dbAmount);
                        logger.debug("Set order ID: {} totalAmount from database: {}", order.getId(), dbAmount);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error retrieving totalAmount for order ID {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
