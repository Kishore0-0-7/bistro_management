package com.bistro.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for Data Access Objects.
 * Defines common CRUD operations for entity types.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's identifier
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Save an entity to the database.
     *
     * @param entity the entity to save
     * @return the saved entity with its ID populated
     * @throws Exception if a database error occurs
     */
    T save(T entity) throws Exception;
    
    /**
     * Update an existing entity in the database.
     *
     * @param entity the entity to update
     * @return the updated entity
     * @throws Exception if a database error occurs
     */
    T update(T entity) throws Exception;
    
    /**
     * Delete an entity from the database by its ID.
     *
     * @param id the ID of the entity to delete
     * @return true if the entity was deleted, false otherwise
     * @throws Exception if a database error occurs
     */
    boolean delete(ID id) throws Exception;
    
    /**
     * Find an entity by its ID.
     *
     * @param id the ID of the entity to find
     * @return an Optional containing the entity if found, or empty if not found
     * @throws Exception if a database error occurs
     */
    Optional<T> findById(ID id) throws Exception;
    
    /**
     * Find all entities.
     *
     * @return a list of all entities
     * @throws Exception if a database error occurs
     */
    List<T> findAll() throws Exception;
}
