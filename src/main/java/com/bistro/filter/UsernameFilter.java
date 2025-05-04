package com.bistro.filter;

import com.bistro.dao.UserDAO;
import com.bistro.dao.impl.UserDAOImpl;
import com.bistro.model.User;
import com.bistro.service.UserService;
import com.bistro.service.impl.UserServiceImpl;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Filter that extracts username from cookies and ensures it's available in the session
 * This helps maintain user context even if session attributes are lost
 */
@WebFilter(urlPatterns = {"/api/*"})
public class UsernameFilter implements Filter {

    private final UserService userService;
    
    public UsernameFilter() {
        this.userService = new UserServiceImpl();
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(true);
        
        // Skip if user is already identified in the session
        if (session.getAttribute("userId") != null) {
            System.out.println("User already identified in session, skipping username filter");
            chain.doFilter(request, response);
            return;
        }
        
        // Check for username in cookies
        String usernameFromCookie = extractUsernameFromCookies(httpRequest);
        if (usernameFromCookie != null) {
            System.out.println("Found username in cookie: " + usernameFromCookie);
            
            // Store username in session
            session.setAttribute("username", usernameFromCookie);
            
            // Try to get user ID from database
            try {
                Optional<User> userOpt = userService.getUserByUsername(usernameFromCookie);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("user", user);
                    session.setAttribute("role", user.getRole());
                    System.out.println("Restored user session from cookie: " + user.getId() + ", " + user.getUsername());
                }
            } catch (Exception e) {
                System.out.println("Error retrieving user by username: " + e.getMessage());
            }
        }
        
        // Continue with request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
    
    /**
     * Extract username from cookies
     */
    private String extractUsernameFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("bistro_username".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
} 