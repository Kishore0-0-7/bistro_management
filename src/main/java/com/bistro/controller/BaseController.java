package com.bistro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bistro.model.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base controller class with common functionality for all controllers.
 */
public abstract class BaseController extends HttpServlet {
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get the request body as a string.
     *
     * @param request the HTTP request
     * @return the request body as a string
     * @throws IOException if an I/O error occurs
     */
    protected String getRequestBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining());
    }
    
    /**
     * Send a JSON response.
     *
     * @param response the HTTP response
     * @param object the object to send as JSON
     * @throws IOException if an I/O error occurs
     */
    protected void sendJsonResponse(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String json = objectMapper.writeValueAsString(object);
        
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
    
    /**
     * Send an error response.
     *
     * @param response the HTTP response
     * @param status the HTTP status code
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    protected void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", message);
        
        sendJsonResponse(response, errorMap);
    }
    
    /**
     * Get the authenticated user from the session.
     *
     * @param request the HTTP request
     * @return the authenticated user, or null if not authenticated
     */
    protected User getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        
        return null;
    }
    
    /**
     * Check if the user is authenticated.
     *
     * @param request the HTTP request
     * @return true if the user is authenticated, false otherwise
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return getAuthenticatedUser(request) != null;
    }
    
    /**
     * Check if the user has the specified role.
     *
     * @param request the HTTP request
     * @param role the role to check
     * @return true if the user has the specified role, false otherwise
     */
    protected boolean hasRole(HttpServletRequest request, String role) {
        User user = getAuthenticatedUser(request);
        
        if (user != null) {
            return role.equals(user.getRole());
        }
        
        return false;
    }
}
