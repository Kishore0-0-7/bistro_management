// Authentication script

// DOM Elements
const loginBtn = document.getElementById('login-btn');
const registerBtn = document.getElementById('register-btn');
const logoutBtn = document.getElementById('logout-btn');
const userProfile = document.getElementById('user-profile');
const usernameDisplay = document.getElementById('username-display');
const loginModal = document.getElementById('login-modal');
const registerModal = document.getElementById('register-modal');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const showRegisterLink = document.getElementById('show-register');
const showLoginLink = document.getElementById('show-login');
const adminNavLink = document.getElementById('admin');
const ordersNavLink = document.getElementById('orders-link');

// Initialize authentication
document.addEventListener('DOMContentLoaded', () => {
    console.log('Auth script initializing...');
    
    // Set up event listeners
    setupAuthEventListeners();
    
    // Check if user is already logged in
    checkAuthentication();
});

// Set up authentication event listeners
function setupAuthEventListeners() {
    console.log('Setting up auth event listeners...');
    
    // Login button
    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            showModal('login-modal');
        });
    }
    
    // Register button
    if (registerBtn) {
        registerBtn.addEventListener('click', () => {
            showModal('register-modal');
        });
    }
    
    // Logout button
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            logout();
        });
    }
    
    // Show register link
    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', (e) => {
            e.preventDefault();
            closeAllModals();
            showModal('register-modal');
        });
    }
    
    // Show login link
    if (showLoginLink) {
        showLoginLink.addEventListener('click', (e) => {
            e.preventDefault();
            closeAllModals();
            showModal('login-modal');
        });
    }
    
    // Login form submission
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();
            
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;
            
            login(username, password);
        });
    }
    
    // Register form submission
    if (registerForm) {
        registerForm.addEventListener('submit', (e) => {
            e.preventDefault();
            
            const username = document.getElementById('register-username').value;
            const email = document.getElementById('register-email').value;
            const password = document.getElementById('register-password').value;
            const confirmPassword = document.getElementById('register-confirm-password').value;
            
            if (password !== confirmPassword) {
                alert('Passwords do not match');
                return;
            }
            
            register(username, email, password);
        });
    }
}

// Check if user is authenticated
function checkAuthentication() {
    console.log('Checking authentication...');
    
    // Check if we just logged out
    const loggedOut = sessionStorage.getItem('logged_out');
    if (loggedOut === 'true') {
        console.log('User recently logged out, skipping auto authentication');
        sessionStorage.removeItem('logged_out'); // Clear the flag
        showUnauthenticatedUI();
        return;
    }
    
    // Check if a logout was performed recently (within 5 seconds)
    const logoutTimestamp = localStorage.getItem('logout_timestamp');
    if (logoutTimestamp) {
        const logoutTime = parseInt(logoutTimestamp, 10);
        const currentTime = Date.now();
        const timeSinceLogout = currentTime - logoutTime;
        
        if (timeSinceLogout < 5000) { // 5 seconds
            console.log('Recent logout detected, skipping auto authentication');
            showUnauthenticatedUI();
            return;
        } else {
            // Clear old logout timestamp
            localStorage.removeItem('logout_timestamp');
        }
    }
    
    // Check for user in session storage first
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (user) {
        console.log('User found in session storage:', user);
        showAuthenticatedUI(user);
    } else {
        console.log('No user found in session storage, checking server...');
        // Try to get user from server session
        fetch('api/auth/check')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to check authentication');
                }
                return response.json();
            })
            .then(data => {
                if (data.authenticated) {
                    // Double check if a logout was requested
                    if (sessionStorage.getItem('logged_out') === 'true') {
                        console.log('Logout flag detected during auth check, ignoring server auth');
                        sessionStorage.removeItem('logged_out');
                        showUnauthenticatedUI();
                        return;
                    }
                    
                    console.log('User authenticated by server:', data.user);
                    // Store user in session storage
                    sessionStorage.setItem('user', JSON.stringify(data.user));
                    showAuthenticatedUI(data.user);
                } else {
                    console.log('User not authenticated by server');
                    showUnauthenticatedUI();
                }
            })
            .catch(error => {
                console.error('Error checking authentication:', error);
                showUnauthenticatedUI();
            });
    }
}

// Login function
function login(username, password) {
    console.log('Attempting login for user:', username);
    
    // Clear any logged_out flag
    sessionStorage.removeItem('logged_out');
    
    // Store the username in a cookie that will persist even if sessionStorage is cleared
    document.cookie = `bistro_username=${username}; path=/; max-age=86400`;
    
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
            throw new Error('Login failed');
        }
        return response.json();
    })
    .then(data => {
        console.log('Login successful:', data);
        // Store user in session storage
        sessionStorage.setItem('user', JSON.stringify(data.user));
        
        // Also store username separately for easier access
        sessionStorage.setItem('username', username);
        
        // Close login modal
        closeAllModals();
        
        // Show authenticated UI
        showAuthenticatedUI(data.user);
        
        // Sync cart with server to associate items with user
        syncCartWithServer();
        
        // Show success message
        alert('Login successful!');
    })
    .catch(error => {
        console.error('Error during login:', error);
        
        // For demonstration, simulate login if API fails
        if (username === 'admin' && password === 'admin123') {
            const user = {
                id: 1,
                username: 'admin',
                email: 'admin@bistro.com',
                role: 'ADMIN',
                firstName: 'Admin',
                lastName: 'User'
            };
            
            console.log('Demo login as admin');
            // Clear logged_out flag
            sessionStorage.removeItem('logged_out');
            
            // Store user in session storage
            sessionStorage.setItem('user', JSON.stringify(user));
            sessionStorage.setItem('username', username);
            
            // Close login modal
            closeAllModals();
            
            // Show authenticated UI
            showAuthenticatedUI(user);
            
            // Show success message
            alert('Login successful! (Demo mode)');
        } else if (username === 'staff' && password === 'staff123') {
            const user = {
                id: 2,
                username: 'staff',
                email: 'staff@bistro.com',
                role: 'STAFF',
                firstName: 'Staff',
                lastName: 'User'
            };
            
            console.log('Demo login as staff');
            // Clear logged_out flag
            sessionStorage.removeItem('logged_out');
            
            // Store user in session storage
            sessionStorage.setItem('user', JSON.stringify(user));
            sessionStorage.setItem('username', username);
            
            // Close login modal
            closeAllModals();
            
            // Show authenticated UI
            showAuthenticatedUI(user);
            
            // Show success message
            alert('Login successful! (Demo mode)');
        } else {
            alert('Invalid username or password');
        }
    });
}

// Sync cart with server
function syncCartWithServer() {
    console.log('Syncing cart with server after login');
    
    // First, ensure credentials are included
    const credentials = {
        username: sessionStorage.getItem('username')
    };
    
    // First, ensure session has the user ID association by making a simple GET request
    fetch('api/cart-service', {
        credentials: 'include' // Ensure cookies are sent with the request
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to initialize cart session');
        }
        return response.json();
    })
    .then(initialData => {
        console.log('Initial cart session established:', initialData);
        
        // Check for pending cart item
        const pendingItem = sessionStorage.getItem('pendingCartItem');
        if (pendingItem) {
            console.log('Found pending cart item to add after login');
            const item = JSON.parse(pendingItem);
            
            // Create cart item payload
            const cartItem = {
                menuItemId: item.id,
                quantity: 1
            };
            
            // Add item to cart
            return fetch('api/cart-service', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(cartItem)
            })
            .then(response => response.json())
            .then(data => {
                console.log('Added pending item to cart:', data);
                // Clear pending item
                sessionStorage.removeItem('pendingCartItem');
                return data;
            });
        }
        return initialData;
    })
    .then(data => {
        // Explicitly sync cart with user account using PUT /sync endpoint
        if (data.items && data.items.length > 0) {
            console.log('Existing cart found, syncing with user account');
            // Force a sync to associate with user
            return fetch('api/cart-service/sync', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data.items)
            });
        }
    })
    .then(response => {
        if (response && !response.ok) {
            throw new Error('Failed to sync cart with server');
        }
        if (response) return response.json();
        return null;
    })
    .then(() => {
        console.log('Cart synced successfully');
        // Update cart count
        if (typeof updateCartCount === 'function') {
            updateCartCount();
        }
    })
    .catch(error => {
        console.error('Error syncing cart with server:', error);
    });
}

// Register function
function register(username, email, password) {
    console.log('Attempting registration for user:', username);
    
    fetch('api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: username,
            email: email,
            password: password
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Registration failed');
        }
        return response.json();
    })
    .then(data => {
        console.log('Registration successful:', data);
        // Close register modal
        closeAllModals();
        
        // Show success message
        alert('Registration successful! Please login.');
        
        // Show login modal
        showModal('login-modal');
    })
    .catch(error => {
        console.error('Error during registration:', error);
        
        // For demonstration, simulate registration if API fails
        closeAllModals();
        alert('Registration successful! Please login. (Demo mode)');
        showModal('login-modal');
    });
}

// Logout function
function logout() {
    console.log('Logging out user');
    
    // Prevent auto-login on page reload
    localStorage.setItem('logout_timestamp', Date.now().toString());
    
    // Set logged_out flag to prevent auto re-login
    sessionStorage.setItem('logged_out', 'true');
    
    // Clear the local session storage 
    sessionStorage.removeItem('user');
    sessionStorage.removeItem('username');
    
    // Clear the bistro_username cookie that might be causing auto-login
    document.cookie = "bistro_username=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    
    // Clear any other cookies that might be related to authentication
    document.cookie = "JSESSIONID=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    
    // Log all cookies for debugging
    console.log('Cookies after clearing:', document.cookie);
    
    // Show unauthenticated UI immediately
    showUnauthenticatedUI();
        
    // Now try to clear the server-side session
    fetch('api/auth/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include' // Include cookies for proper session handling
    })
    .then(response => {
        console.log('Logout response received, status:', response.status);
        
        // Handle the response - but we've already cleared local storage
        if (!response.ok) {
            console.warn('Server-side logout failed, but local session was cleared');
            
            // Force redirect to home page with cache-busting parameter
            setTimeout(function() {
                window.location.href = 'index.html?nocache=' + new Date().getTime();
            }, 500);
            return null;
        }
        return response.json();
    })
    .then(data => {
        if (data) {
            console.log('Logout successful, response data:', data);
        }
        
        // Force redirect to home page with cache-busting parameter
        setTimeout(function() {
            window.location.href = 'index.html?nocache=' + new Date().getTime();
        }, 500);
    })
    .catch(error => {
        console.error('Error during logout:', error);
        
        // Force redirect to home page with cache-busting parameter
        setTimeout(function() {
            window.location.href = 'index.html?nocache=' + new Date().getTime();
        }, 500);
    });
}

// Show authenticated UI
function showAuthenticatedUI(user) {
    console.log('Showing authenticated UI for user:', user);
    
    if (!userProfile || !usernameDisplay || !loginBtn || !registerBtn || !logoutBtn) {
        console.error('Required DOM elements for auth UI not found');
        return;
    }
    
    // Hide login and register buttons
    loginBtn.classList.add('hidden');
    registerBtn.classList.add('hidden');
    
    // Show user profile
    userProfile.classList.remove('hidden');
    
    // Set username
    usernameDisplay.textContent = user.username;
    
    // Show orders link for all authenticated users
    if (ordersNavLink) {
        ordersNavLink.classList.remove('hidden');
    } else {
        console.error('Orders nav link element not found');
    }
    
    // Show admin link if user is admin or staff
    if (adminNavLink) {
        if (user.role === 'ADMIN' || user.role === 'STAFF') {
            console.log('Showing admin link for role:', user.role);
            adminNavLink.classList.remove('hidden');
        } else {
            adminNavLink.classList.add('hidden');
        }
    } else {
        console.error('Admin nav link element not found');
    }
}

// Show unauthenticated UI
function showUnauthenticatedUI() {
    console.log('Showing unauthenticated UI');
    
    if (!userProfile || !loginBtn || !registerBtn || !logoutBtn) {
        console.error('Required DOM elements for auth UI not found');
        return;
    }
    
    // Show login and register buttons
    loginBtn.classList.remove('hidden');
    registerBtn.classList.remove('hidden');
    
    // Hide user profile
    userProfile.classList.add('hidden');
    
    // Hide orders link
    if (ordersNavLink) {
        ordersNavLink.classList.add('hidden');
    } else {
        console.error('Orders nav link element not found');
    }
    
    // Hide admin link
    if (adminNavLink) {
        adminNavLink.classList.add('hidden');
    } else {
        console.error('Admin nav link element not found');
    }
    
    // Check if current page is restricted
    const currentPage = window.location.pathname.split('/').pop();
    if (currentPage === 'orders.html' || currentPage === 'admin.html') {
        // Redirect to home page if on restricted page
        window.location.href = 'home.html';
    }
}

// Show modal
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    } else {
        console.error(`Modal not found: ${modalId}`);
    }
}

// Close all modals
function closeAllModals() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
}

// Show section (helper function)
function showSection(sectionId) {
    console.log('Auth showSection called for:', sectionId);
    
    // Use the global showSection function if available
    if (typeof window.showSection === 'function' && window.showSection !== showSection) {
        window.showSection(sectionId);
    } else {
        console.error('Global showSection function not available, using fallback');
        // Fallback: manually show section
        const sections = document.querySelectorAll('.section');
        sections.forEach(section => {
            section.classList.remove('active');
        });
        
        const targetSection = document.getElementById(sectionId);
        if (targetSection) {
            targetSection.classList.add('active');
        }
    }
}
