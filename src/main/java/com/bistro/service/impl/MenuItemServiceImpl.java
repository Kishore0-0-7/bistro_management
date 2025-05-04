package com.bistro.service.impl;

import com.bistro.dao.MenuItemDAO;
import com.bistro.dao.impl.MenuItemDAOImpl;
import com.bistro.model.MenuItem;
import com.bistro.service.MenuItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the MenuItemService interface.
 */
public class MenuItemServiceImpl implements MenuItemService {
    private static final Logger logger = LoggerFactory.getLogger(MenuItemServiceImpl.class);
    private final MenuItemDAO menuItemDAO;
    
    public MenuItemServiceImpl() {
        this.menuItemDAO = new MenuItemDAOImpl();
    }
    
    @Override
    public MenuItem addMenuItem(MenuItem menuItem) throws Exception {
        logger.info("Adding new menu item: {}", menuItem.getName());
        return menuItemDAO.save(menuItem);
    }
    
    @Override
    public MenuItem updateMenuItem(MenuItem menuItem) throws Exception {
        // Check if menu item exists
        Optional<MenuItem> existingItemOpt = menuItemDAO.findById(menuItem.getId());
        
        if (existingItemOpt.isEmpty()) {
            logger.warn("Update failed: Menu item with ID {} not found", menuItem.getId());
            throw new Exception("Menu item not found");
        }
        
        logger.info("Updating menu item: {}", menuItem.getName());
        return menuItemDAO.update(menuItem);
    }
    
    @Override
    public boolean deleteMenuItem(int id) throws Exception {
        // Check if menu item exists
        Optional<MenuItem> existingItemOpt = menuItemDAO.findById(id);
        
        if (existingItemOpt.isEmpty()) {
            logger.warn("Delete failed: Menu item with ID {} not found", id);
            throw new Exception("Menu item not found");
        }
        
        logger.info("Deleting menu item with ID: {}", id);
        return menuItemDAO.delete(id);
    }
    
    @Override
    public Optional<MenuItem> getMenuItemById(int id) throws Exception {
        logger.debug("Getting menu item by ID: {}", id);
        return menuItemDAO.findById(id);
    }
    
    @Override
    public List<MenuItem> getAllMenuItems() throws Exception {
        logger.debug("Getting all menu items");
        return menuItemDAO.findAll();
    }
    
    @Override
    public List<MenuItem> getMenuItemsByCategory(String category) throws Exception {
        logger.debug("Getting menu items by category: {}", category);
        return menuItemDAO.findByCategory(category);
    }
    
    @Override
    public List<MenuItem> getFeaturedMenuItems() throws Exception {
        logger.debug("Getting featured menu items");
        return menuItemDAO.findFeatured();
    }
    
    @Override
    public List<MenuItem> getAvailableMenuItems() throws Exception {
        logger.debug("Getting available menu items");
        return menuItemDAO.findAvailable();
    }
    
    @Override
    public List<MenuItem> searchMenuItems(String query) throws Exception {
        logger.debug("Searching menu items with query: {}", query);
        return menuItemDAO.search(query);
    }
    
    @Override
    public List<String> getAllCategories() throws Exception {
        logger.debug("Getting all menu categories");
        return menuItemDAO.getAllCategories();
    }
    
    @Override
    public MenuItem toggleAvailability(int id) throws Exception {
        // Get the menu item
        Optional<MenuItem> menuItemOpt = menuItemDAO.findById(id);
        
        if (menuItemOpt.isEmpty()) {
            logger.warn("Toggle availability failed: Menu item with ID {} not found", id);
            throw new Exception("Menu item not found");
        }
        
        MenuItem menuItem = menuItemOpt.get();
        
        // Toggle availability
        boolean newAvailability = !menuItem.isAvailable();
        menuItem.setAvailable(newAvailability);
        
        logger.info("Toggling availability for menu item {}: {}", menuItem.getName(), newAvailability);
        
        // Update the menu item
        return menuItemDAO.update(menuItem);
    }
    
    @Override
    public MenuItem toggleFeatured(int id) throws Exception {
        // Get the menu item
        Optional<MenuItem> menuItemOpt = menuItemDAO.findById(id);
        
        if (menuItemOpt.isEmpty()) {
            logger.warn("Toggle featured failed: Menu item with ID {} not found", id);
            throw new Exception("Menu item not found");
        }
        
        MenuItem menuItem = menuItemOpt.get();
        
        // Toggle featured status
        boolean newFeaturedStatus = !menuItem.isFeatured();
        menuItem.setFeatured(newFeaturedStatus);
        
        logger.info("Toggling featured status for menu item {}: {}", menuItem.getName(), newFeaturedStatus);
        
        // Update the menu item
        return menuItemDAO.update(menuItem);
    }
}
