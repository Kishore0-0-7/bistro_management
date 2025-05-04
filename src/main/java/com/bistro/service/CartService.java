package com.bistro.service;

import com.bistro.model.CartItem;
import com.bistro.model.MenuItem;
import com.bistro.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

public class CartService {
    
    /**
     * Get or create a cart for the current session
     */
    public static int getOrCreateCart(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        
        System.out.println("CartService.getOrCreateCart - Using session: " + sessionId);
        
        // Get user ID if logged in
        Integer userId = null;
        String username = null;
        
        // First try to get the userId attribute directly
        if (session.getAttribute("userId") != null) {
            userId = (Integer) session.getAttribute("userId");
            System.out.println("CartService.getOrCreateCart - Using userId: " + userId + " from session attribute");
        } 
        // If not found, try to get from user object
        else {
            System.out.println("CartService.getOrCreateCart - No userId attribute found in session, looking for user object");
            Object userObj = session.getAttribute("user");
            if (userObj != null) {
                System.out.println("CartService.getOrCreateCart - User object found in session: " + userObj);
                try {
                    // Try to get the user ID from the user object using reflection
                    java.lang.reflect.Method getIdMethod = userObj.getClass().getMethod("getId");
                    Object result = getIdMethod.invoke(userObj);
                    if (result instanceof Integer) {
                        userId = (Integer) result;
                        // Store the userId in session for future use
                        session.setAttribute("userId", userId);
                        System.out.println("CartService.getOrCreateCart - Extracted userId: " + userId + " from user object and stored in session");
                    }
                    
                    // Also try to get username
                    java.lang.reflect.Method getUsernameMethod = userObj.getClass().getMethod("getUsername");
                    Object usernameResult = getUsernameMethod.invoke(userObj);
                    if (usernameResult instanceof String) {
                        username = (String) usernameResult;
                        System.out.println("CartService.getOrCreateCart - Extracted username: " + username + " from user object");
                    }
                } catch (Exception e) {
                    System.out.println("CartService.getOrCreateCart - Error extracting info from user object: " + e.getMessage());
                }
            } else {
                System.out.println("CartService.getOrCreateCart - No user object found in session, using sessionId only: " + sessionId);
            }
        }
        
        // Try to get username from session directly if not found from user object
        if (username == null && session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
            System.out.println("CartService.getOrCreateCart - Found username in session: " + username);
        }
        
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // If username is available but no userId, try to get userId from the database
            if (userId == null && username != null) {
                try (PreparedStatement userIdQuery = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                    userIdQuery.setString(1, username);
                    try (ResultSet userIdRs = userIdQuery.executeQuery()) {
                        if (userIdRs.next()) {
                            userId = userIdRs.getInt("id");
                            session.setAttribute("userId", userId);
                            System.out.println("CartService.getOrCreateCart - Found userId from database using username: " + userId);
                        }
                    }
                }
            }
            
            // If user is logged in, check if they already have a cart
            if (userId != null) {
                // First try to find a cart by user ID
                try (PreparedStatement cartPs = conn.prepareStatement(
                        "SELECT id FROM carts WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1")) {
                    cartPs.setInt(1, userId);
                    try (ResultSet cartRs = cartPs.executeQuery()) {
                        if (cartRs.next()) {
                            int existingCartId = cartRs.getInt("id");
                            System.out.println("CartService.getOrCreateCart - Found existing cart with ID: " + existingCartId + " for user: " + userId);
                            
                            // Update the session ID to make sure it's current
                            try (PreparedStatement updatePs = conn.prepareStatement(
                                    "UPDATE carts SET session_id = ?, updated_at = NOW() WHERE id = ?")) {
                                updatePs.setString(1, sessionId);
                                updatePs.setInt(2, existingCartId);
                                updatePs.executeUpdate();
                                System.out.println("CartService.getOrCreateCart - Updated session ID for cart: " + existingCartId);
                            }
                            
                            return existingCartId;
                        }
                    }
                }
            }
            
            // Try to find a cart by session ID
            try (PreparedStatement sessionCartPs = conn.prepareStatement(
                    "SELECT id FROM carts WHERE session_id = ? ORDER BY updated_at DESC LIMIT 1")) {
                sessionCartPs.setString(1, sessionId);
                try (ResultSet sessionCartRs = sessionCartPs.executeQuery()) {
                    if (sessionCartRs.next()) {
                        int existingCartId = sessionCartRs.getInt("id");
                        System.out.println("CartService.getOrCreateCart - Found existing cart with ID: " + existingCartId + " for session: " + sessionId);
                        
                        // If user is now logged in, update the user ID
                        if (userId != null) {
                            try (PreparedStatement updateUserPs = conn.prepareStatement(
                                    "UPDATE carts SET user_id = ?, updated_at = NOW() WHERE id = ?")) {
                                updateUserPs.setInt(1, userId);
                                updateUserPs.setInt(2, existingCartId);
                                updateUserPs.executeUpdate();
                                System.out.println("CartService.getOrCreateCart - Updated user ID for cart: " + existingCartId + " to user: " + userId);
                            }
                        }
                        
                        return existingCartId;
                    }
                }
            }
            
            // No existing cart found, create a new one
            try (PreparedStatement createPs = conn.prepareStatement(
                    "INSERT INTO carts (session_id, user_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                    Statement.RETURN_GENERATED_KEYS)) {
                createPs.setString(1, sessionId);
                if (userId != null) {
                    createPs.setInt(2, userId);
                } else {
                    createPs.setNull(2, Types.INTEGER);
                }
                
                int rowsAffected = createPs.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = createPs.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newCartId = generatedKeys.getInt(1);
                            System.out.println("CartService.getOrCreateCart - Created new cart with ID: " + newCartId + 
                                    (userId != null ? " for user: " + userId : " for session: " + sessionId));
                            return newCartId;
                        }
                    }
                }
            }
            
            throw new SQLException("Failed to create a new cart");
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Find cart by user ID
     */
    public static int findCartByUserId(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("SELECT id FROM carts WHERE user_id = ? ORDER BY updated_at DESC LIMIT 1");
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1; // No cart found
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(stmt);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Force update cart user ID
     */
    private static void forceUpdateCartUserId(int cartId, int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement("UPDATE carts SET user_id = ? WHERE id = ?");
            stmt.setInt(1, userId);
            stmt.setInt(2, cartId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Forced update of user ID for cart " + cartId + ": " + rowsAffected + " rows affected");
        } finally {
            DBUtil.closeStatement(stmt);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Add an item to the cart
     */
    public static void addToCart(int cartId, int menuItemId, int quantity) throws SQLException {
        System.out.println("CartService.addToCart - Adding item " + menuItemId + " to cart " + cartId + " with quantity " + quantity);
        
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement updatePs = null;
        PreparedStatement insertPs = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // First check if the item already exists in the cart
            checkPs = conn.prepareStatement(
                    "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND menu_item_id = ?");
            checkPs.setInt(1, cartId);
            checkPs.setInt(2, menuItemId);
            
            rs = checkPs.executeQuery();
            
            if (rs.next()) {
                // Item exists, update quantity
                int existingId = rs.getInt("id");
                int existingQuantity = rs.getInt("quantity");
                int newQuantity = existingQuantity + quantity;
                
                updatePs = conn.prepareStatement(
                        "UPDATE cart_items SET quantity = ?, updated_at = NOW() WHERE id = ?");
                updatePs.setInt(1, newQuantity);
                updatePs.setInt(2, existingId);
                
                int rowsUpdated = updatePs.executeUpdate();
                System.out.println("CartService.addToCart - Updated existing item, rows affected: " + rowsUpdated);
            } else {
                // Item doesn't exist, insert new
                insertPs = conn.prepareStatement(
                        "INSERT INTO cart_items (cart_id, menu_item_id, quantity, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())");
                insertPs.setInt(1, cartId);
                insertPs.setInt(2, menuItemId);
                insertPs.setInt(3, quantity);
                
                int rowsInserted = insertPs.executeUpdate();
                System.out.println("CartService.addToCart - Inserted new item, rows affected: " + rowsInserted);
            }
            
            // Update cart's updated_at timestamp
            try (PreparedStatement cartPs = conn.prepareStatement(
                    "UPDATE carts SET updated_at = NOW() WHERE id = ?")) {
                cartPs.setInt(1, cartId);
                cartPs.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error adding item to cart: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(checkPs);
            DBUtil.closeStatement(updatePs);
            DBUtil.closeStatement(insertPs);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Update an item's quantity in the cart
     */
    public static void updateCartItem(int cartId, int menuItemId, int quantity) throws SQLException {
        System.out.println("CartService.updateCartItem - Updating item " + menuItemId + " in cart " + cartId + " with quantity change " + quantity);
        
        Connection conn = null;
        PreparedStatement checkPs = null;
        PreparedStatement updatePs = null;
        PreparedStatement deletePs = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // First check if the item exists and get current quantity
            checkPs = conn.prepareStatement(
                    "SELECT id, quantity FROM cart_items WHERE cart_id = ? AND menu_item_id = ?");
            checkPs.setInt(1, cartId);
            checkPs.setInt(2, menuItemId);
            
            rs = checkPs.executeQuery();
            
            if (rs.next()) {
                int itemId = rs.getInt("id");
                int currentQuantity = rs.getInt("quantity");
                int newQuantity = currentQuantity + quantity;
                
                System.out.println("CartService.updateCartItem - Current quantity: " + currentQuantity + ", New quantity: " + newQuantity);
                
                if (newQuantity <= 0) {
                    // Delete the item if quantity becomes zero or negative
                    deletePs = conn.prepareStatement("DELETE FROM cart_items WHERE id = ?");
                    deletePs.setInt(1, itemId);
                    
                    int rowsDeleted = deletePs.executeUpdate();
                    System.out.println("CartService.updateCartItem - Deleted item (quantity <= 0), rows affected: " + rowsDeleted);
                } else {
                    // Update the quantity
                    updatePs = conn.prepareStatement(
                            "UPDATE cart_items SET quantity = ?, updated_at = NOW() WHERE id = ?");
                    updatePs.setInt(1, newQuantity);
                    updatePs.setInt(2, itemId);
                    
                    int rowsUpdated = updatePs.executeUpdate();
                    System.out.println("CartService.updateCartItem - Updated item quantity, rows affected: " + rowsUpdated);
                }
                
                // Update cart's updated_at timestamp
                try (PreparedStatement cartPs = conn.prepareStatement(
                        "UPDATE carts SET updated_at = NOW() WHERE id = ?")) {
                    cartPs.setInt(1, cartId);
                    cartPs.executeUpdate();
                }
            } else {
                System.out.println("CartService.updateCartItem - Item not found in cart");
                
                // If the item doesn't exist and we're trying to add it (positive quantity)
                if (quantity > 0) {
                    addToCart(cartId, menuItemId, quantity);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating cart item: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(checkPs);
            DBUtil.closeStatement(updatePs);
            DBUtil.closeStatement(deletePs);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Remove an item from the cart
     */
    public static void removeFromCart(int cartId, int menuItemId) throws SQLException {
        System.out.println("CartService.removeFromCart - Removing item " + menuItemId + " from cart " + cartId);
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // Delete the item from cart
            ps = conn.prepareStatement(
                    "DELETE FROM cart_items WHERE cart_id = ? AND menu_item_id = ?");
            ps.setInt(1, cartId);
            ps.setInt(2, menuItemId);
            
            int rowsDeleted = ps.executeUpdate();
            System.out.println("CartService.removeFromCart - Removed item, rows affected: " + rowsDeleted);
            
            // Update cart's updated_at timestamp
            try (PreparedStatement cartPs = conn.prepareStatement(
                    "UPDATE carts SET updated_at = NOW() WHERE id = ?")) {
                cartPs.setInt(1, cartId);
                cartPs.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error removing item from cart: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            DBUtil.closeStatement(ps);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Clear all items from the cart
     */
    public static void clearCart(int cartId) throws SQLException {
        System.out.println("CartService.clearCart - Clearing all items from cart " + cartId);
        
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // Delete all items from cart
            ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?");
            ps.setInt(1, cartId);
            
            int rowsDeleted = ps.executeUpdate();
            System.out.println("CartService.clearCart - Cleared cart, items removed: " + rowsDeleted);
            
            // Update cart's updated_at timestamp
            try (PreparedStatement cartPs = conn.prepareStatement(
                    "UPDATE carts SET updated_at = NOW() WHERE id = ?")) {
                cartPs.setInt(1, cartId);
                cartPs.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            DBUtil.closeStatement(ps);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Get all items in a cart
     */
    public static List<CartItem> getCartItems(int cartId) throws SQLException {
        System.out.println("CartService.getCartItems - Looking up items for cart ID: " + cartId);
        List<CartItem> cartItems = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // Use direct SQL query instead of stored procedure
            String sql = "SELECT ci.id, ci.cart_id, ci.menu_item_id, ci.quantity, " +
                         "mi.name, mi.description, mi.price, mi.image_url " +
                         "FROM cart_items ci " +
                         "JOIN menu_items mi ON ci.menu_item_id = mi.id " +
                         "WHERE ci.cart_id = ?";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cartId);
            
            rs = ps.executeQuery();
            int count = 0;
            
            while (rs.next()) {
                count++;
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setCartId(rs.getInt("cart_id"));
                item.setMenuItemId(rs.getInt("menu_item_id"));
                item.setQuantity(rs.getInt("quantity"));
                
                MenuItem menuItem = new MenuItem();
                menuItem.setId(rs.getInt("menu_item_id"));
                menuItem.setName(rs.getString("name"));
                menuItem.setPrice(rs.getBigDecimal("price"));
                menuItem.setDescription(rs.getString("description"));
                menuItem.setImageUrl(rs.getString("image_url"));
                
                item.setMenuItem(menuItem);
                
                // Calculate total for this item
                BigDecimal itemPrice = menuItem.getPrice();
                BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(item.getQuantity()));
                item.setTotal(itemTotal);
                
                cartItems.add(item);
            }
            
            System.out.println("CartService.getCartItems - Found " + count + " items for cart ID: " + cartId);
            return cartItems;
        } catch (SQLException e) {
            System.err.println("Error getting cart items: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(ps);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Get the total price of all items in the cart
     */
    public static double getCartTotal(int cartId) throws SQLException {
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareCall("{CALL usp_get_cart_total(?)}");
            stmt.setInt(1, cartId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            } else {
                return 0.0;
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(stmt);
            DBUtil.closeConnection(conn);
        }
    }
    
    /**
     * Synchronize cart between local storage and database
     */
    public static void syncCartWithDatabase(HttpServletRequest request, List<CartItem> localCartItems) throws SQLException {
        int cartId = getOrCreateCart(request);
        
        // Clear existing cart in database
        clearCart(cartId);
        
        // Add all items from local storage to database
        for (CartItem item : localCartItems) {
            addToCart(cartId, item.getMenuItemId(), item.getQuantity());
        }
    }
} 