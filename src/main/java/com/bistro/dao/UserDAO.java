package com.bistro.dao;

import com.bistro.model.User;
import java.util.Optional;

/**
 * Interface for User data access operations.
 * Extends the BaseDAO with User-specific methods.
 */
public interface UserDAO extends BaseDAO<User, Integer> {
    
    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, or empty if not found
     * @throws Exception if a database error occurs
     */
    Optional<User> findByUsername(String username) throws Exception;
    
    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty if not found
     * @throws Exception if a database error occurs
     */
    Optional<User> findByEmail(String email) throws Exception;
    
    /**
     * Check if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     * @throws Exception if a database error occurs
     */
    boolean existsByUsername(String username) throws Exception;
    
    /**
     * Check if an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     * @throws Exception if a database error occurs
     */
    boolean existsByEmail(String email) throws Exception;
}
