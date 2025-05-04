package com.bistro.dao.impl;

import com.bistro.dao.MenuItemDAO;
import com.bistro.model.MenuItem;
import com.bistro.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the MenuItemDAO interface for database operations related to menu items.
 */
public class MenuItemDAOImpl implements MenuItemDAO {
    private static final Logger logger = LoggerFactory.getLogger(MenuItemDAOImpl.class);

    @Override
    public MenuItem save(MenuItem menuItem) throws Exception {
        String sql = "INSERT INTO menu_items (name, description, price, category, image_url, available, featured, preparation_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, menuItem.getName());
            stmt.setString(2, menuItem.getDescription());
            stmt.setBigDecimal(3, menuItem.getPrice());
            stmt.setString(4, menuItem.getCategory());
            stmt.setString(5, menuItem.getImageUrl());
            stmt.setBoolean(6, menuItem.isAvailable());
            stmt.setBoolean(7, menuItem.isFeatured());
            stmt.setInt(8, menuItem.getPreparationTime());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating menu item failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menuItem.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating menu item failed, no ID obtained.");
                }
            }
            
            return menuItem;
        } catch (SQLException e) {
            logger.error("Error saving menu item: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public MenuItem update(MenuItem menuItem) throws Exception {
        String sql = "UPDATE menu_items SET name = ?, description = ?, price = ?, category = ?, " +
                     "image_url = ?, available = ?, featured = ?, preparation_time = ? " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, menuItem.getName());
            stmt.setString(2, menuItem.getDescription());
            stmt.setBigDecimal(3, menuItem.getPrice());
            stmt.setString(4, menuItem.getCategory());
            stmt.setString(5, menuItem.getImageUrl());
            stmt.setBoolean(6, menuItem.isAvailable());
            stmt.setBoolean(7, menuItem.isFeatured());
            stmt.setInt(8, menuItem.getPreparationTime());
            stmt.setInt(9, menuItem.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating menu item failed, no rows affected.");
            }
            
            return menuItem;
        } catch (SQLException e) {
            logger.error("Error updating menu item: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws Exception {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting menu item: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<MenuItem> findById(Integer id) throws Exception {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMenuItem(rs));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding menu item by ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<MenuItem> findAll() throws Exception {
        String sql = "SELECT * FROM menu_items";
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                menuItems.add(mapResultSetToMenuItem(rs));
            }
            
            return menuItems;
        } catch (SQLException e) {
            logger.error("Error finding all menu items: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<MenuItem> findByCategory(String category) throws Exception {
        String sql = "SELECT * FROM menu_items WHERE category = ?";
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    menuItems.add(mapResultSetToMenuItem(rs));
                }
            }
            
            return menuItems;
        } catch (SQLException e) {
            logger.error("Error finding menu items by category: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<MenuItem> findFeatured() throws Exception {
        String sql = "SELECT * FROM menu_items WHERE featured = true AND available = true";
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                menuItems.add(mapResultSetToMenuItem(rs));
            }
            
            return menuItems;
        } catch (SQLException e) {
            logger.error("Error finding featured menu items: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<MenuItem> findAvailable() throws Exception {
        String sql = "SELECT * FROM menu_items WHERE available = true";
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                menuItems.add(mapResultSetToMenuItem(rs));
            }
            
            return menuItems;
        } catch (SQLException e) {
            logger.error("Error finding available menu items: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<MenuItem> search(String query) throws Exception {
        String sql = "SELECT * FROM menu_items WHERE name LIKE ? OR description LIKE ?";
        List<MenuItem> menuItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchQuery = "%" + query + "%";
            stmt.setString(1, searchQuery);
            stmt.setString(2, searchQuery);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    menuItems.add(mapResultSetToMenuItem(rs));
                }
            }
            
            return menuItems;
        } catch (SQLException e) {
            logger.error("Error searching menu items: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<String> getAllCategories() throws Exception {
        String sql = "SELECT DISTINCT category FROM menu_items ORDER BY category";
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
            return categories;
        } catch (SQLException e) {
            logger.error("Error getting all categories: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Maps a ResultSet row to a MenuItem object.
     *
     * @param rs the ResultSet to map
     * @return a MenuItem object
     * @throws SQLException if a database error occurs
     */
    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(rs.getInt("id"));
        menuItem.setName(rs.getString("name"));
        menuItem.setDescription(rs.getString("description"));
        menuItem.setPrice(rs.getBigDecimal("price"));
        menuItem.setCategory(rs.getString("category"));
        menuItem.setImageUrl(rs.getString("image_url"));
        menuItem.setAvailable(rs.getBoolean("available"));
        menuItem.setFeatured(rs.getBoolean("featured"));
        menuItem.setPreparationTime(rs.getInt("preparation_time"));
        return menuItem;
    }
}
