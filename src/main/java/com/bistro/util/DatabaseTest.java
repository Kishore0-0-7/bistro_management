package com.bistro.util;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * A simple utility class to test database connectivity and check if the default admin user exists.
 * Run this class to verify database setup.
 */
public class DatabaseTest {
    
    public static void main(String[] args) {
        System.out.println("Testing database connection and default admin user...");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("Database connection successful!");
            
            // Check if the users table exists
            boolean usersTableExists = tableExists(conn, "users");
            System.out.println("Users table exists: " + usersTableExists);
            
            if (!usersTableExists) {
                System.out.println("Creating users table...");
                createUsersTable(conn);
                System.out.println("Users table created successfully!");
            }
            
            // Check if admin user exists
            boolean adminExists = adminUserExists(conn);
            System.out.println("Admin user exists: " + adminExists);
            
            if (!adminExists) {
                System.out.println("Creating default admin user...");
                createDefaultAdmin(conn);
            } else {
                System.out.println("Admin user details:");
                printAdminDetails(conn);
                
                // Test authentication with admin user
                System.out.println("\nTesting authentication with admin user...");
                boolean authSuccess = testAuthentication(conn, "admin", "admin123");
                System.out.println("Authentication successful: " + authSuccess);
                
                if (!authSuccess) {
                    System.out.println("Updating admin password...");
                    updateAdminPassword(conn);
                    System.out.println("Admin password updated to 'admin123'");
                }
            }
            
            // Check other tables
            checkAndCreateTables(conn);
            
            System.out.println("\nDatabase setup complete and verified!");
            
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null);
        return rs.next();
    }
    
    private static boolean adminUserExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    private static void printAdminDetails(Connection conn) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = 'admin'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Role: " + rs.getString("role"));
                System.out.println("Password Hash: " + rs.getString("password_hash"));
            }
        }
    }
    
    private static void createUsersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "first_name VARCHAR(50), " +
                "last_name VARCHAR(50), " +
                "phone VARCHAR(20), " +
                "address TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    private static void createDefaultAdmin(Connection conn) throws SQLException {
        // BCrypt hash for password "admin123"
        String passwordHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        
        String sql = "INSERT INTO users (username, email, password_hash, role, first_name, last_name, created_at, updated_at) " +
                     "VALUES ('admin', 'admin@bistro.com', ?, 'ADMIN', 'Admin', 'User', ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            Date now = new Date();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(now.getTime());
            stmt.setTimestamp(2, timestamp);
            stmt.setTimestamp(3, timestamp);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Default admin user created successfully!");
            } else {
                System.out.println("Failed to create default admin user.");
            }
        }
    }
    
    private static boolean testAuthentication(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return BCrypt.checkpw(password, storedHash);
            }
        }
        return false;
    }
    
    private static void updateAdminPassword(Connection conn) throws SQLException {
        // BCrypt hash for password "admin123"
        String passwordHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        
        String sql = "UPDATE users SET password_hash = ? WHERE username = 'admin'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.executeUpdate();
        }
    }
    
    private static void checkAndCreateTables(Connection conn) throws SQLException {
        // Check and create menu_items table
        if (!tableExists(conn, "menu_items")) {
            System.out.println("Creating menu_items table...");
            String sql = "CREATE TABLE menu_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "image_url VARCHAR(255), " +
                    "available BOOLEAN DEFAULT TRUE, " +
                    "featured BOOLEAN DEFAULT FALSE, " +
                    "preparation_time INT DEFAULT 15" +
                    ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("menu_items table created successfully!");
                
                // Insert sample menu items
                insertSampleMenuItems(conn);
            }
        } else {
            System.out.println("menu_items table exists");
        }
        
        // Check and create orders table
        if (!tableExists(conn, "orders")) {
            System.out.println("Creating orders table...");
            String sql = "CREATE TABLE orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "total_amount DECIMAL(10, 2) NOT NULL, " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "delivery_date TIMESTAMP NULL, " +
                    "delivery_address TEXT, " +
                    "payment_method VARCHAR(50) NOT NULL, " +
                    "payment_status VARCHAR(20) NOT NULL, " +
                    "special_instructions TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("orders table created successfully!");
            }
        } else {
            System.out.println("orders table exists");
        }
        
        // Check and create order_items table
        if (!tableExists(conn, "order_items")) {
            System.out.println("Creating order_items table...");
            String sql = "CREATE TABLE order_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id INT NOT NULL, " +
                    "menu_item_id INT NOT NULL, " +
                    "menu_item_name VARCHAR(100) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "special_instructions TEXT, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)" +
                    ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("order_items table created successfully!");
            }
        } else {
            System.out.println("order_items table exists");
        }
    }
    
    private static void insertSampleMenuItems(Connection conn) throws SQLException {
        String sql = "INSERT INTO menu_items (name, description, price, category, image_url, available, featured, preparation_time) VALUES " +
                "('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'Pizza', 'images/menu/margherita.jpg', TRUE, TRUE, 20), " +
                "('Pepperoni Pizza', 'Pizza with tomato sauce, mozzarella, and pepperoni', 14.99, 'Pizza', 'images/menu/pepperoni.jpg', TRUE, FALSE, 20), " +
                "('Vegetarian Pizza', 'Pizza with tomato sauce, mozzarella, and assorted vegetables', 13.99, 'Pizza', 'images/menu/vegetarian.jpg', TRUE, FALSE, 25), " +
                "('Caesar Salad', 'Romaine lettuce, croutons, parmesan cheese, and Caesar dressing', 8.99, 'Salad', 'images/menu/caesar.jpg', TRUE, TRUE, 10), " +
                "('Greek Salad', 'Mixed greens, feta cheese, olives, tomatoes, and Greek dressing', 9.99, 'Salad', 'images/menu/greek.jpg', TRUE, FALSE, 10), " +
                "('Garden Salad', 'Mixed greens, tomatoes, cucumbers, and balsamic dressing', 7.99, 'Salad', 'images/menu/garden.jpg', TRUE, FALSE, 10), " +
                "('Spaghetti Bolognese', 'Spaghetti with meat sauce', 14.99, 'Pasta', 'images/menu/bolognese.jpg', TRUE, TRUE, 25), " +
                "('Fettuccine Alfredo', 'Fettuccine with creamy Alfredo sauce', 13.99, 'Pasta', 'images/menu/alfredo.jpg', TRUE, FALSE, 20), " +
                "('Lasagna', 'Layered pasta with meat sauce and cheese', 15.99, 'Pasta', 'images/menu/lasagna.jpg', TRUE, FALSE, 30)";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            
            // Insert more menu items
            sql = "INSERT INTO menu_items (name, description, price, category, image_url, available, featured, preparation_time) VALUES " +
                    "('Grilled Chicken', 'Grilled chicken breast with vegetables and mashed potatoes', 16.99, 'Main Course', 'images/menu/grilled_chicken.jpg', TRUE, TRUE, 25), " +
                    "('Beef Steak', 'Grilled beef steak with vegetables and fries', 22.99, 'Main Course', 'images/menu/beef_steak.jpg', TRUE, FALSE, 30), " +
                    "('Salmon Fillet', 'Grilled salmon fillet with vegetables and rice', 19.99, 'Main Course', 'images/menu/salmon.jpg', TRUE, FALSE, 25), " +
                    "('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 6.99, 'Dessert', 'images/menu/chocolate_cake.jpg', TRUE, TRUE, 5), " +
                    "('Cheesecake', 'Creamy cheesecake with strawberry topping', 7.99, 'Dessert', 'images/menu/cheesecake.jpg', TRUE, FALSE, 5), " +
                    "('Ice Cream', 'Vanilla ice cream with chocolate sauce', 4.99, 'Dessert', 'images/menu/ice_cream.jpg', TRUE, FALSE, 5), " +
                    "('Coca-Cola', 'Classic Coca-Cola', 2.99, 'Beverage', 'images/menu/coke.jpg', TRUE, FALSE, 2), " +
                    "('Sprite', 'Refreshing Sprite', 2.99, 'Beverage', 'images/menu/sprite.jpg', TRUE, FALSE, 2), " +
                    "('Orange Juice', 'Freshly squeezed orange juice', 3.99, 'Beverage', 'images/menu/orange_juice.jpg', TRUE, FALSE, 5)";
            
            stmt.executeUpdate(sql);
            
            System.out.println("Sample menu items inserted successfully!");
        }
    }
}
