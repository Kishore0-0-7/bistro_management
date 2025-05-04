package com.bistro.service.impl;

import com.bistro.dao.UserDAO;
import com.bistro.dao.impl.UserDAOImpl;
import com.bistro.model.User;
import com.bistro.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the UserService interface.
 */
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;
    
    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }
    
    @Override
    public User register(User user) throws Exception {
        // Check if username or email already exists
        if (userDAO.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username {} already exists", user.getUsername());
            throw new Exception("Username already exists");
        }
        
        if (userDAO.existsByEmail(user.getEmail())) {
            logger.warn("Registration failed: Email {} already exists", user.getEmail());
            throw new Exception("Email already exists");
        }
        
        // Check if a password is provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.warn("Registration failed: Password is required");
            throw new Exception("Password is required");
        }
        
        // Hash the password
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CUSTOMER");
        }
        
        // Set created and updated timestamps
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        // Save the user
        User savedUser = userDAO.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }
    
    @Override
    public Optional<User> authenticate(String username, String password) throws Exception {
        // Find user by username
        Optional<User> userOpt = userDAO.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if password matches
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                logger.info("User authenticated successfully: {}", username);
                return userOpt;
            }
        }
        
        logger.warn("Authentication failed for username: {}", username);
        return Optional.empty();
    }
    
    @Override
    public Optional<User> getUserById(int id) throws Exception {
        logger.debug("Getting user by ID: {}", id);
        return userDAO.findById(id);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) throws Exception {
        logger.debug("Getting user by username: {}", username);
        return userDAO.findByUsername(username);
    }
    
    @Override
    public Optional<User> getUserByEmail(String email) throws Exception {
        logger.debug("Getting user by email: {}", email);
        return userDAO.findByEmail(email);
    }
    
    @Override
    public User updateProfile(User user) throws Exception {
        // Get existing user
        Optional<User> existingUserOpt = userDAO.findById(user.getId());
        
        if (existingUserOpt.isEmpty()) {
            logger.warn("Update profile failed: User with ID {} not found", user.getId());
            throw new Exception("User not found");
        }
        
        User existingUser = existingUserOpt.get();
        
        // Check if username is being changed and is already taken
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            userDAO.existsByUsername(user.getUsername())) {
            logger.warn("Update profile failed: New username {} already exists", user.getUsername());
            throw new Exception("Username already exists");
        }
        
        // Check if email is being changed and is already taken
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userDAO.existsByEmail(user.getEmail())) {
            logger.warn("Update profile failed: New email {} already exists", user.getEmail());
            throw new Exception("Email already exists");
        }
        
        // Keep the existing password hash
        user.setPasswordHash(existingUser.getPasswordHash());
        
        // Update the timestamp
        user.setUpdatedAt(new Date());
        
        // Update the user
        User updatedUser = userDAO.update(user);
        logger.info("User profile updated successfully: {}", updatedUser.getUsername());
        return updatedUser;
    }
    
    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        // Get existing user
        Optional<User> userOpt = userDAO.findById(userId);
        
        if (userOpt.isEmpty()) {
            logger.warn("Change password failed: User with ID {} not found", userId);
            throw new Exception("User not found");
        }
        
        User user = userOpt.get();
        
        // Check if old password matches
        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            logger.warn("Change password failed: Incorrect old password for user {}", user.getUsername());
            return false;
        }
        
        // Hash the new password
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        
        // Update the timestamp
        user.setUpdatedAt(new Date());
        
        // Update the user
        userDAO.update(user);
        logger.info("Password changed successfully for user: {}", user.getUsername());
        
        return true;
    }
    
    @Override
    public boolean setPassword(int userId, String newPassword) throws Exception {
        // Get existing user
        Optional<User> userOpt = userDAO.findById(userId);
        
        if (userOpt.isEmpty()) {
            logger.warn("Set password failed: User with ID {} not found", userId);
            throw new Exception("User not found");
        }
        
        User user = userOpt.get();
        
        // Hash the new password
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        
        // Update the timestamp
        user.setUpdatedAt(new Date());
        
        // Update the user
        userDAO.update(user);
        logger.info("Password set by admin for user: {}", user.getUsername());
        
        return true;
    }
    
    @Override
    public List<User> getAllUsers() throws Exception {
        logger.debug("Getting all users");
        return userDAO.findAll();
    }
    
    @Override
    public boolean isUsernameAvailable(String username) throws Exception {
        boolean available = !userDAO.existsByUsername(username);
        logger.debug("Username {} availability check: {}", username, available);
        return available;
    }
    
    @Override
    public boolean isEmailAvailable(String email) throws Exception {
        boolean available = !userDAO.existsByEmail(email);
        logger.debug("Email {} availability check: {}", email, available);
        return available;
    }
    
    @Override
    public boolean deleteUser(int userId) throws Exception {
        logger.debug("Deleting user with ID: {}", userId);
        
        // First check if the user exists
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("Delete user failed: User with ID {} not found", userId);
            return false;
        }
        
        User user = userOpt.get();
        
        // Don't delete users with ADMIN role for safety
        if ("ADMIN".equals(user.getRole())) {
            logger.warn("Delete user failed: Cannot delete user with ADMIN role, ID: {}", userId);
            throw new Exception("Cannot delete users with ADMIN role");
        }
        
        // Delete the user
        boolean deleted = userDAO.delete(userId);
        
        if (deleted) {
            logger.info("User with ID {} deleted successfully", userId);
        } else {
            logger.error("Failed to delete user with ID {}", userId);
        }
        
        return deleted;
    }
}
