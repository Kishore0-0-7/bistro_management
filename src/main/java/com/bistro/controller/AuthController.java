package com.bistro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.bistro.model.User;
import com.bistro.service.UserService;
import com.bistro.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling authentication-related requests.
 */
@WebServlet("/api/auth/*")
public class AuthController extends BaseController {
    private final UserService userService;
    
    public AuthController() {
        this.userService = new UserServiceImpl();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
            return;
        }
        
        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/register":
                handleRegister(request, response);
                break;
            case "/logout":
                handleLogout(request, response);
                break;
            default:
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                break;
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if ("/check".equals(pathInfo)) {
            handleCheckAuth(request, response);
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
    
    /**
     * Handle login request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = getRequestBody(request);
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            
            Optional<User> userOpt = userService.authenticate(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Create session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());
                session.setAttribute("userId", user.getId());
                
                // Prepare response
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("message", "Login successful");
                responseMap.put("user", user);
                
                sendJsonResponse(response, responseMap);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during login: " + e.getMessage());
        }
    }
    
    /**
     * Handle register request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestBody = getRequestBody(request);
            User user = objectMapper.readValue(requestBody, User.class);
            
            // Register the user
            User registeredUser = userService.register(user);
            
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", registeredUser);
            session.setAttribute("role", registeredUser.getRole());
            session.setAttribute("userId", registeredUser.getId());
            
            // Prepare response
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Registration successful");
            responseMap.put("user", registeredUser);
            
            sendJsonResponse(response, responseMap);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error during registration: " + e.getMessage());
        }
    }
    
    /**
     * Handle logout request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            session.invalidate();
        }
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Logout successful");
        
        sendJsonResponse(response, responseMap);
    }
    
    /**
     * Handle check authentication request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    private void handleCheckAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getAuthenticatedUser(request);
        
        Map<String, Object> responseMap = new HashMap<>();
        
        if (user != null) {
            // Make sure userId is set in session
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") == null) {
                session.setAttribute("userId", user.getId());
            }
            
            responseMap.put("authenticated", true);
            responseMap.put("user", user);
        } else {
            responseMap.put("authenticated", false);
        }
        
        sendJsonResponse(response, responseMap);
    }
}
