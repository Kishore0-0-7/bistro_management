package com.bistro.service;

import com.bistro.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for User-related operations.
 */
public interface UserService {
    
    /**
     * Register a new user.
     *
     * @param user the user to register
     * @return the registered user with ID
     * @throws Exception if registration fails
     */
    User register(User user) throws Exception;
    
    /**
     * Authenticate a user with username and password.
     *
     * @param username the username
     * @param password the password (plain text)
     * @return an Optional containing the authenticated user if successful, or empty if authentication fails
     * @throws Exception if authentication fails
     */
    Optional<User> authenticate(String username, String password) throws Exception;
    
    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return an Optional containing the user if found, or empty if not found
     * @throws Exception if retrieval fails
     */
    Optional<User> getUserById(int id) throws Exception;
    
    /**
     * Get a user by username.
     *
     * @param username the username
     * @return an Optional containing the user if found, or empty if not found
     * @throws Exception if retrieval fails
     */
    Optional<User> getUserByUsername(String username) throws Exception;
    
    /**
     * Get a user by email.
     *
     * @param email the email
     * @return an Optional containing the user if found, or empty if not found
     * @throws Exception if retrieval fails
     */
    Optional<User> getUserByEmail(String email) throws Exception;
    
    /**
     * Update a user's profile.
     *
     * @param user the user with updated information
     * @return the updated user
     * @throws Exception if update fails
     */
    User updateProfile(User user) throws Exception;
    
    /**
     * Change a user's password.
     *
     * @param userId the user ID
     * @param oldPassword the old password (plain text)
     * @param newPassword the new password (plain text)
     * @return true if the password was changed, false otherwise
     * @throws Exception if password change fails
     */
    boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception;
    
    /**
     * Get all users.
     *
     * @return a list of all users
     * @throws Exception if retrieval fails
     */
    List<User> getAllUsers() throws Exception;
    
    /**
     * Check if a username is available.
     *
     * @param username the username to check
     * @return true if the username is available, false otherwise
     * @throws Exception if check fails
     */
    boolean isUsernameAvailable(String username) throws Exception;
    
    /**
     * Check if an email is available.
     *
     * @param email the email to check
     * @return true if the email is available, false otherwise
     * @throws Exception if check fails
     */
    boolean isEmailAvailable(String email) throws Exception;
    
    /**
     * Set a new password for a user (admin only).
     * This method doesn't require old password verification.
     *
     * @param userId the user ID
     * @param newPassword the new password (plain text)
     * @return true if the password was changed, false otherwise
     * @throws Exception if password change fails
     */
    boolean setPassword(int userId, String newPassword) throws Exception;

    /**
     * Delete a user by ID.
     *
     * @param userId the user ID to delete
     * @return true if the user was deleted, false otherwise
     * @throws Exception if deletion fails
     */
    boolean deleteUser(int userId) throws Exception;
}
