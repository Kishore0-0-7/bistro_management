/**
 * Common Utility Functions
 * These functions are used across the application for common tasks
 */

// Load browser compatibility script immediately
(function loadCompatibility() {
    if (!document.getElementById('browser-compatibility-script')) {
        var script = document.createElement('script');
        script.id = 'browser-compatibility-script';
        script.src = (window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')) || '') + '/js/browser-compatibility.js';
        document.head.appendChild(script);
    }
})();

// API URL base path
const API_BASE_URL = window.location.origin + 
    (window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')) || '');

// Common API fetch function with error handling
async function fetchApi(endpoint, options = {}) {
    try {
        // Normalize the path to handle different systems
        endpoint = normalizeApiPath(endpoint);
        
        // Set default headers
        if (!options.headers) {
            options.headers = {
                'Content-Type': 'application/json'
            };
        }
        
        // Add credentials for session cookies
        options.credentials = 'same-origin';
        
        // Make the request
        const response = await fetch(`${API_BASE_URL}/api${endpoint}`, options);
        
        // Check if response is OK
        if (!response.ok) {
            // Try to parse error message from JSON response
            try {
                const errorData = await response.json();
                throw new Error(errorData.message || `API Error: ${response.status}`);
            } catch (jsonError) {
                throw new Error(`API Error: ${response.status} - ${response.statusText}`);
            }
        }
        
        // If response is 204 No Content, return null
        if (response.status === 204) {
            return null;
        }
        
        // Otherwise parse JSON
        return await response.json();
    } catch (error) {
        console.error('API Request Failed:', error);
        
        // Re-throw error to be handled by caller
        throw error;
    }
}

// Show notification
function showNotification(message, type = 'info', duration = 3000) {
    // Create notification element if it doesn't exist
    let notification = document.getElementById('notification');
    if (!notification) {
        notification = document.createElement('div');
        notification.id = 'notification';
        document.body.appendChild(notification);
        
        // Add basic styling if not defined in CSS
        if (!document.getElementById('notification-style')) {
            const style = document.createElement('style');
            style.id = 'notification-style';
            style.textContent = `
                #notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    padding: 10px 20px;
                    border-radius: 4px;
                    color: white;
                    font-weight: bold;
                    z-index: 9999;
                    opacity: 0;
                    transition: opacity 0.3s ease;
                }
                #notification.visible {
                    opacity: 1;
                }
                #notification.info {
                    background-color: #2196F3;
                }
                #notification.success {
                    background-color: #4CAF50;
                }
                #notification.warning {
                    background-color: #FF9800;
                }
                #notification.error {
                    background-color: #F44336;
                }
            `;
            document.head.appendChild(style);
        }
    }
    
    // Set message and type
    notification.textContent = message;
    notification.className = type;
    
    // Show notification
    setTimeout(() => notification.classList.add('visible'), 10);
    
    // Hide after duration
    setTimeout(() => {
        notification.classList.remove('visible');
        setTimeout(() => {
            notification.textContent = '';
        }, 300);
    }, duration);
}

// Format currency
function formatCurrency(amount) {
    if (amount === undefined || amount === null) {
        return '0.00';
    }
    
    return parseFloat(amount).toFixed(2);
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '';
    
    const options = { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    
    try {
        return new Date(dateString).toLocaleString('en-US', options);
    } catch (e) {
        console.error('Error formatting date:', e);
        return dateString;
    }
}

// Sanitize HTML to prevent XSS
function sanitizeHtml(html) {
    const temp = document.createElement('div');
    temp.textContent = html;
    return temp.innerHTML;
}

// Get URL parameter
function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    const regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    const results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
}

// Check if user is authenticated
function isAuthenticated() {
    return !!getCurrentUser();
}

// Get current user from session storage
function getCurrentUser() {
    try {
        return JSON.parse(sessionStorage.getItem('user'));
    } catch (e) {
        console.error('Error parsing user data:', e);
        return null;
    }
}

// Check if user has admin access
function hasAdminAccess() {
    const user = getCurrentUser();
    return user && (user.role === 'ADMIN' || user.role === 'STAFF');
}

// Redirect to login if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = (window.baseUrl || '') + '/home.html?redirect=' + encodeURIComponent(window.location.pathname);
        return false;
    }
    return true;
}

// Debounce function for input handlers
function debounce(func, wait = 300) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
} 