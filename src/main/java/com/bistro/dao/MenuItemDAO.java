package com.bistro.dao;

import com.bistro.model.MenuItem;
import java.util.List;

/**
 * Interface for MenuItem data access operations.
 * Extends the BaseDAO with MenuItem-specific methods.
 */
public interface MenuItemDAO extends BaseDAO<MenuItem, Integer> {
    
    /**
     * Find menu items by category.
     *
     * @param category the category to search for
     * @return a list of menu items in the specified category
     * @throws Exception if a database error occurs
     */
    List<MenuItem> findByCategory(String category) throws Exception;
    
    /**
     * Find featured menu items.
     *
     * @return a list of featured menu items
     * @throws Exception if a database error occurs
     */
    List<MenuItem> findFeatured() throws Exception;
    
    /**
     * Find available menu items.
     *
     * @return a list of available menu items
     * @throws Exception if a database error occurs
     */
    List<MenuItem> findAvailable() throws Exception;
    
    /**
     * Search menu items by name or description.
     *
     * @param query the search query
     * @return a list of menu items matching the search query
     * @throws Exception if a database error occurs
     */
    List<MenuItem> search(String query) throws Exception;
    
    /**
     * Get all distinct categories of menu items.
     *
     * @return a list of distinct categories
     * @throws Exception if a database error occurs
     */
    List<String> getAllCategories() throws Exception;
}
