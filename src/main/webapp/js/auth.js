// Authentication Module

// DOM Elements
let loginForm, registerForm, loginModal, registerModal;
let loginBtn, registerBtn, logoutBtn, userProfile, usernameDisplay, userAvatar;
let ordersLink, adminLink;
let showRegisterLink, showLoginLink;

// Initialize auth module
document.addEventListener('DOMContentLoaded', function() {
    // Get DOM elements
    initAuthElements();
    
    // Set up event listeners
    setupAuthListeners();
    
    // Check authentication status
    checkAuth();
});

// Initialize auth elements
function initAuthElements() {
    // Forms and modals
    loginForm = document.getElementById('login-form');
    registerForm = document.getElementById('register-form');
    loginModal = document.getElementById('login-modal');
    registerModal = document.getElementById('register-modal');
    
    // Buttons and displays
    loginBtn = document.getElementById('login-btn');
    registerBtn = document.getElementById('register-btn');
    logoutBtn = document.getElementById('logout-btn');
    userProfile = document.getElementById('user-profile');
    usernameDisplay = document.getElementById('username-display');
    userAvatar = document.querySelector('.user-avatar');
    
    // Navigation links
    ordersLink = document.getElementById('orders-link');
    adminLink = document.getElementById('admin-link');
    
    // Modal links
    showRegisterLink = document.getElementById('show-register');
    showLoginLink = document.getElementById('show-login');
}

// Set up auth event listeners
function setupAuthListeners() {
    // Login form submission
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            login();
        });
    }
    
    // Register form submission
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            register();
        });
    }
    
    // Login button
    if (loginBtn) {
        loginBtn.addEventListener('click', function() {
            openModal(loginModal);
        });
    }
    
    // Register button
    if (registerBtn) {
        registerBtn.addEventListener('click', function() {
            openModal(registerModal);
        });
    }
    
    // Logout button
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            logout();
        });
    }
    
    // Show register link
    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', function(e) {
            e.preventDefault();
            closeModal(loginModal);
            openModal(registerModal);
        });
    }
    
    // Show login link
    if (showLoginLink) {
        showLoginLink.addEventListener('click', function(e) {
            e.preventDefault();
            closeModal(registerModal);
            openModal(loginModal);
        });
    }
    
    // Close modals
    document.querySelectorAll('.close-modal').forEach(function(closeBtn) {
        closeBtn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });
    
    // Close modal on outside click
    window.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeModal(e.target);
        }
    });
}

// Check authentication status
function checkAuth() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (user) {
        // User is logged in
        if (loginBtn) loginBtn.classList.add('hidden');
        if (registerBtn) registerBtn.classList.add('hidden');
        if (userProfile) userProfile.classList.remove('hidden');
        
        // Update user display
        if (usernameDisplay) {
            usernameDisplay.textContent = user.username;
        }
        
        // Initialize avatar display with user's first initial
        updateUserAvatar();
        
        // Show navigation links based on role
        if (ordersLink) ordersLink.classList.remove('hidden');
        
        if (user.role === 'ADMIN' || user.role === 'STAFF') {
            if (adminLink) adminLink.classList.remove('hidden');
        }
    } else {
        // User is not logged in
        if (loginBtn) loginBtn.classList.remove('hidden');
        if (registerBtn) registerBtn.classList.remove('hidden');
        if (userProfile) userProfile.classList.add('hidden');
        
        // Hide navigation links
        if (ordersLink) ordersLink.classList.add('hidden');
        if (adminLink) adminLink.classList.add('hidden');
    }
}

// Login function
function login() {
    // Get form data
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    // Disable submit button
    const submitBtn = loginForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';
    
    // Login request
    fetch('api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    .then(response => {
        if (!response.ok) {
            if(response.status === 401) {
                throw new Error('Invalid username or password');
            } else {
                throw new Error('Login failed. Please try again.');
            }
        }
        return response.json();
    })
    .then(data => {
        // Store user data in session storage
        sessionStorage.setItem('user', JSON.stringify(data.user));
        
        // Close modal
        closeModal(loginModal);
        
        // Update UI
        checkAuth();
        
        // Update user avatar
        updateUserAvatar();
        
        // Reset form
        loginForm.reset();
        
        // Show success notification
        showNotification('Login Successful', 'Welcome back, ' + data.user.username + '!', 'success');
        
        // Refresh page if needed
        if (window.location.pathname.includes('profile.html') || 
            window.location.pathname.includes('orders.html') ||
            (window.location.pathname.includes('admin.html') && (data.user.role === 'ADMIN' || data.user.role === 'STAFF'))) {
            window.location.reload();
        }
    })
    .catch(error => {
        console.error('Login error:', error);
        showNotification('Login Failed', error.message, 'error');
    })
    .finally(() => {
        // Re-enable submit button
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login';
    });
}

// Register function
function register() {
    // Get form data
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;
    
    // Validate passwords
    if (password !== confirmPassword) {
        showNotification('Registration Error', 'Passwords do not match', 'error');
        return;
    }
    
    // Disable submit button
    const submitBtn = registerForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Registering...';
    
    // Register request
    fetch('api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            email: email,
            password: password  // Using 'password' field as expected by the backend
        })
    })
    .then(response => {
        if (!response.ok) {
            if (response.headers.get('Content-Type')?.includes('application/json')) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Registration failed');
                });
            } else {
                throw new Error('Registration failed: ' + response.status);
            }
        }
        return response.json();
    })
    .then(data => {
        // Close modal
        closeModal(registerModal);
        
        // Show success notification
        showNotification('Registration Successful', 'Your account has been created successfully. You can now log in.', 'success');
        
        // Reset form
        registerForm.reset();
        
        // Open login modal
        setTimeout(() => {
            openModal(loginModal);
        }, 1500);
    })
    .catch(error => {
        console.error('Registration error:', error);
        showNotification('Registration Failed', error.message, 'error');
    })
    .finally(() => {
        // Re-enable submit button
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register';
    });
}

// Logout function
function logout() {
    // Clear session storage
    sessionStorage.removeItem('user');
    
    // Update UI
    checkAuth();
    
    // Show notification
    showNotification('Logout Successful', 'You have been logged out successfully', 'info');
    
    // Redirect to home if on protected page
    if (window.location.pathname.includes('profile.html') || 
        window.location.pathname.includes('orders.html') ||
        window.location.pathname.includes('admin.html')) {
        window.location.href = 'home.html';
    }
}

// Open modal
function openModal(modal) {
    if (modal) {
        modal.style.display = 'block';
    }
}

// Close modal
function closeModal(modal) {
    if (modal) {
        modal.style.display = 'none';
    }
}

// Show notification
function showNotification(title, message, type = 'info') {
    // Create notification container if it doesn't exist
    let notificationContainer = document.querySelector('.notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.className = 'notification-container';
        document.body.appendChild(notificationContainer);
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Create notification content
    notification.innerHTML = `
        <div class="notification-header">
            <h3>${title}</h3>
            <span class="notification-close">&times;</span>
        </div>
        <div class="notification-body">
            <p>${message}</p>
        </div>
    `;
    
    // Add to container
    notificationContainer.appendChild(notification);
    
    // Add close button functionality
    const closeBtn = notification.querySelector('.notification-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            notification.remove();
        });
    }
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

// Update user avatar display
function updateUserAvatar() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (!user) return;
    
    // Get all user avatar elements
    const avatars = document.querySelectorAll('.user-avatar');
    
    // Exit if no avatars found
    if (!avatars || avatars.length === 0) return;
    
    // Use first letter of username for avatar
    const initial = user.username ? user.username.charAt(0).toUpperCase() : '';
    
    if (initial) {
        // Get role-based class
        const roleClass = user.role ? user.role.toLowerCase() : 'customer';
        
        // Update all avatars in the document
        avatars.forEach(avatar => {
            // Clear existing content
            avatar.innerHTML = '';
            
            // Remove any existing role classes
            avatar.classList.remove('admin', 'staff', 'customer');
            
            // Add role-based class
            avatar.classList.add(roleClass);
            
            // Create text node for the initial
            const textNode = document.createTextNode(initial);
            avatar.appendChild(textNode);
        });
    } else {
        // Fallback to icon
        avatars.forEach(avatar => {
            avatar.innerHTML = '<i class="fas fa-user"></i>';
            
            // Remove any existing role classes
            avatar.classList.remove('admin', 'staff', 'customer');
            
            // Add default class
            avatar.classList.add('customer');
        });
    }
}
