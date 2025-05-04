// Main application script

// DOM Elements
const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
const mainNav = document.getElementById('main-nav');
const orderNowBtn = document.getElementById('order-now-btn');
const cartBtn = document.getElementById('cart-btn');
const cartCount = document.getElementById('cart-count');
const modals = document.querySelectorAll('.modal');
const closeButtons = document.querySelectorAll('.close-modal');

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    console.log('App initializing...');
    
    // Check if user is logged in
    checkAuthentication();
    
    // Set up event listeners
    setupEventListeners();
    
    // Load cart count
    updateCartCount();
    
    // Load page-specific content
    loadPageContent();
});

// Set up event listeners
function setupEventListeners() {
    console.log('Setting up event listeners...');
    
    // Mobile menu toggle
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', () => {
            console.log('Mobile menu toggle clicked');
            if (mainNav) {
                mainNav.classList.toggle('active');
            }
        });
    } else {
        console.error('Mobile menu toggle not found');
    }
    
    // Close mobile menu when window is resized to desktop size
    window.addEventListener('resize', () => {
        if (window.innerWidth > 768 && mainNav) {
            mainNav.classList.remove('active');
        }
    });
    
    // Order now button
    if (orderNowBtn) {
        orderNowBtn.addEventListener('click', () => {
            console.log('Order now button clicked');
            window.location.href = 'menu.html';
        });
    }
    
    // Close modal buttons
    if (closeButtons && closeButtons.length > 0) {
        closeButtons.forEach(button => {
            button.addEventListener('click', () => {
                closeAllModals();
            });
        });
    } else {
        console.error('Close modal buttons not found');
    }
    
    // Close modal when clicking outside
    if (modals && modals.length > 0) {
        window.addEventListener('click', (e) => {
            modals.forEach(modal => {
                if (e.target === modal) {
                    closeAllModals();
                }
            });
        });
    } else {
        console.error('Modals not found');
    }
}

// Load page-specific content
function loadPageContent() {
    // Get current page from URL
    const currentPage = window.location.pathname.split('/').pop();
    console.log('Current page:', currentPage);
    
    // Load content based on page
    switch (currentPage) {
        case 'home.html':
        case '':
        case 'index.html':
            if (typeof loadFeaturedItems === 'function') {
                loadFeaturedItems();
            }
            break;
        case 'menu.html':
            if (typeof loadMenuItems === 'function') {
                loadMenuItems();
            }
            break;
        case 'orders.html':
            // Check if user is logged in
            if (isLoggedIn()) {
                if (typeof loadUserOrders === 'function') {
                    loadUserOrders();
                }
            } else {
                // Redirect to home if not logged in
                alert('Please login to view your orders');
                window.location.href = 'home.html';
            }
            break;
        case 'admin.html':
            // Check if user is admin or staff
            const user = JSON.parse(sessionStorage.getItem('user'));
            if (user && (user.role === 'ADMIN' || user.role === 'STAFF')) {
                if (typeof loadAdminDashboard === 'function') {
                    loadAdminDashboard();
                }
            } else {
                // Redirect to home if not admin or staff
                alert('You do not have permission to access the admin dashboard');
                window.location.href = 'home.html';
            }
            break;
    }
}

// Update cart count
function updateCartCount() {
    // Fetch current cart from server
    fetch('api/cart-service')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            const count = data.items.reduce((total, item) => total + item.quantity, 0);
            
            // Update all cart count elements
            const cartCountElements = document.querySelectorAll('#cart-count');
            cartCountElements.forEach(element => {
                element.textContent = count;
            });
        })
        .catch(error => {
            console.error('Error fetching cart count from server:', error);
            
            // Set cart count to 0 if error
            const cartCountElements = document.querySelectorAll('#cart-count');
            cartCountElements.forEach(element => {
                element.textContent = "0";
            });
        });
}

// Show modal
function showModal(modalId) {
    console.log(`Showing modal: ${modalId}`);
    const modal = document.getElementById(modalId);
    if (modal) {
        console.log(`Found modal element, displaying it`);
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

// Check if user is logged in
function isLoggedIn() {
    return sessionStorage.getItem('user') !== null;
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}
