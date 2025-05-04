package com.bistro.controller;

import com.bistro.model.User;
import com.bistro.service.UserService;
import com.bistro.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling user-specific operations.
 */
@WebServlet("/api/users/*")
public class UserController extends BaseController {
    private final UserService userService;
    
    public UserController() {
        this.userService = new UserServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated
        User user = getAuthenticatedUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/profile")) {
            // Return current user profile
            sendJsonResponse(response, user);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated
        User user = getAuthenticatedUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        try {
            if (pathInfo.equals("/password/change")) {
                handleChangePassword(request, response, user);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // User must be authenticated
        User user = getAuthenticatedUser(request);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/profile")) {
            try {
                handleUpdateProfile(request, response, user);
            } catch (Exception e) {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating profile: " + e.getMessage());
            }
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    /**
     * Handle change password request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param user the authenticated user
     * @throws Exception if an error occurs
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        String requestBody = getRequestBody(request);
        Map<String, String> passwordData = objectMapper.readValue(requestBody, Map.class);
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        // Validate password inputs
        if (oldPassword == null || oldPassword.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Old password is required");
            return;
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "New password is required");
            return;
        }
        
        // Attempt to change password
        boolean success = userService.changePassword(user.getId(), oldPassword, newPassword);
        
        if (success) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Password changed successfully");
            sendJsonResponse(response, responseMap);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid old password");
        }
    }
    
    /**
     * Handle update profile request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param user the authenticated user
     * @throws Exception if an error occurs
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User user) throws Exception {
        String requestBody = getRequestBody(request);
        User updatedUser = objectMapper.readValue(requestBody, User.class);
        
        // Ensure the user is only updating their own profile
        updatedUser.setId(user.getId());
        
        // Don't allow changing role or password through this endpoint
        updatedUser.setRole(user.getRole());
        updatedUser.setPasswordHash(user.getPasswordHash());
        
        // Update the profile
        User savedUser = userService.updateProfile(updatedUser);
        
        // Update session with new user info
        request.getSession().setAttribute("user", savedUser);
        
        // Return updated user
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Profile updated successfully");
        responseMap.put("user", savedUser);
        sendJsonResponse(response, responseMap);
    }
} 