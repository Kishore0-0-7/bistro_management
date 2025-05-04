package com.bistro.service;

import com.bistro.model.MenuItem;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for MenuItem-related operations.
 */
public interface MenuItemService {
    
    /**
     * Add a new menu item.
     *
     * @param menuItem the menu item to add
     * @return the added menu item with ID
     * @throws Exception if addition fails
     */
    MenuItem addMenuItem(MenuItem menuItem) throws Exception;
    
    /**
     * Update an existing menu item.
     *
     * @param menuItem the menu item with updated information
     * @return the updated menu item
     * @throws Exception if update fails
     */
    MenuItem updateMenuItem(MenuItem menuItem) throws Exception;
    
    /**
     * Delete a menu item by ID.
     *
     * @param id the menu item ID
     * @return true if the menu item was deleted, false otherwise
     * @throws Exception if deletion fails
     */
    boolean deleteMenuItem(int id) throws Exception;
    
    /**
     * Get a menu item by ID.
     *
     * @param id the menu item ID
     * @return an Optional containing the menu item if found, or empty if not found
     * @throws Exception if retrieval fails
     */
    Optional<MenuItem> getMenuItemById(int id) throws Exception;
    
    /**
     * Get all menu items.
     *
     * @return a list of all menu items
     * @throws Exception if retrieval fails
     */
    List<MenuItem> getAllMenuItems() throws Exception;
    
    /**
     * Get menu items by category.
     *
     * @param category the category
     * @return a list of menu items in the specified category
     * @throws Exception if retrieval fails
     */
    List<MenuItem> getMenuItemsByCategory(String category) throws Exception;
    
    /**
     * Get featured menu items.
     *
     * @return a list of featured menu items
     * @throws Exception if retrieval fails
     */
    List<MenuItem> getFeaturedMenuItems() throws Exception;
    
    /**
     * Get available menu items.
     *
     * @return a list of available menu items
     * @throws Exception if retrieval fails
     */
    List<MenuItem> getAvailableMenuItems() throws Exception;
    
    /**
     * Search menu items by name or description.
     *
     * @param query the search query
     * @return a list of menu items matching the search query
     * @throws Exception if search fails
     */
    List<MenuItem> searchMenuItems(String query) throws Exception;
    
    /**
     * Get all distinct categories of menu items.
     *
     * @return a list of distinct categories
     * @throws Exception if retrieval fails
     */
    List<String> getAllCategories() throws Exception;
    
    /**
     * Toggle the availability of a menu item.
     *
     * @param id the menu item ID
     * @return the updated menu item
     * @throws Exception if toggle fails
     */
    MenuItem toggleAvailability(int id) throws Exception;
    
    /**
     * Toggle the featured status of a menu item.
     *
     * @param id the menu item ID
     * @return the updated menu item
     * @throws Exception if toggle fails
     */
    MenuItem toggleFeatured(int id) throws Exception;
}
