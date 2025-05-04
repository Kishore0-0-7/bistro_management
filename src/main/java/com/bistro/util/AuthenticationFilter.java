package com.bistro.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter to handle authentication for protected API endpoints.
 * This ensures that only authenticated users can access certain endpoints.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        // Check if user is authenticated
        if (session == null || session.getAttribute("user") == null) {
            // If the request is for admin endpoints, check for admin role
            if (httpRequest.getRequestURI().contains("/api/admin/")) {
                if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.getWriter().write("{\"error\":\"Unauthorized access\"}");
                    return;
                }
            }
            
            // For other protected endpoints, just check if user is logged in
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\":\"Authentication required\"}");
            return;
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
