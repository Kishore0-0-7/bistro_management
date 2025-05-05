// Main Application Script

// First, load the browser compatibility script
document.addEventListener('DOMContentLoaded', function() {
    // Load browser compatibility script
    if (!document.getElementById('browser-compatibility-script')) {
        var script = document.createElement('script');
        script.id = 'browser-compatibility-script';
        script.src = window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')) + '/js/browser-compatibility.js';
        document.head.appendChild(script);
    }
});

// DOM Elements
let mobileMenuToggle, mainNav, mainNavLinks, pageTitle;

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize DOM elements
    initAppElements();
    
    // Set up event listeners
    setupAppEventListeners();
    
    // Initialize mobile menu
    initMobileMenu();
    
    // Highlight current page in navigation
    highlightCurrentPage();
    
    // Update page title based on current page
    updatePageTitle();
    
    // Update user avatars across the site
    updateUserAvatar();
});

// Initialize DOM elements
function initAppElements() {
    // Navigation elements
    mobileMenuToggle = document.getElementById('mobile-menu-toggle');
    mainNav = document.getElementById('main-nav');
    mainNavLinks = document.querySelectorAll('#main-nav a');
    
    // Page title element for dynamic updates
    pageTitle = document.querySelector('title');
}

// Set up app event listeners
function setupAppEventListeners() {
    // Mobile menu toggle
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', toggleMobileMenu);
    }
    
    // Close menu when clicking outside
    document.addEventListener('click', function(e) {
        if (mainNav && mainNav.classList.contains('active') &&
            !e.target.closest('#main-nav') && 
            !e.target.closest('#mobile-menu-toggle')) {
            mainNav.classList.remove('active');
        }
    });
    
    // Add section transition animation for all nav links
    mainNavLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Only handle internal navigation (not external links)
            if (this.getAttribute('href').startsWith('#')) {
                e.preventDefault();
                const targetId = this.getAttribute('href').substring(1);
                showSection(targetId);
            }
        });
    });
}

// Initialize mobile menu
function initMobileMenu() {
    // Add CSS for mobile animation if needed
    if (!document.getElementById('mobile-menu-style')) {
        const style = document.createElement('style');
        style.id = 'mobile-menu-style';
        style.textContent = `
            @media (max-width: 768px) {
                #main-nav {
                    transition: max-height 0.3s ease-in-out;
                    overflow: hidden;
                    max-height: 0;
                }
                #main-nav.active {
                    max-height: 500px;
                }
            }
        `;
        document.head.appendChild(style);
    }
}

// Toggle mobile menu
function toggleMobileMenu() {
    if (mainNav) {
        mainNav.classList.toggle('active');
    }
}

// Show section with smooth transition
function showSection(sectionId) {
    const sections = document.querySelectorAll('.section');
    const targetSection = document.getElementById(sectionId);
    
    if (!targetSection) return;
    
    // Fade out current sections
    sections.forEach(section => {
        if (section.classList.contains('active')) {
            // Add exit animation
            section.style.opacity = '0';
            setTimeout(() => {
                section.classList.remove('active');
                section.style.opacity = '';
            }, 300);
        }
    });
    
    // Fade in target section
    setTimeout(() => {
        targetSection.classList.add('active');
        targetSection.style.opacity = '0';
        
        // Trigger browser reflow
        targetSection.offsetHeight;
        
        // Fade in
        targetSection.style.opacity = '1';
        targetSection.style.transition = 'opacity 0.3s ease-in-out';
        
        // Reset transition after animation
        setTimeout(() => {
            targetSection.style.transition = '';
        }, 300);
    }, 300);
    
    // Update URL hash
    window.location.hash = sectionId;
    
    // Update navigation active state
    updateNavigation(sectionId);
}

// Update navigation active state
function updateNavigation(sectionId) {
    mainNavLinks.forEach(link => {
        link.classList.remove('active');
        
        const linkTarget = link.getAttribute('href');
        if (linkTarget === `#${sectionId}` || 
            (sectionId === 'home' && linkTarget === 'home.html') ||
            linkTarget.endsWith(`${sectionId}.html`)) {
            link.classList.add('active');
        }
    });
}

// Highlight current page in navigation
function highlightCurrentPage() {
    const currentPage = window.location.pathname.split('/').pop() || 'home.html';
    
    mainNavLinks.forEach(link => {
        const href = link.getAttribute('href');
        link.classList.remove('active');
        
        if (href === currentPage || 
            (currentPage === '' && href === 'home.html') ||
            (currentPage === 'index.html' && href === 'home.html')) {
            link.classList.add('active');
        }
    });
}

// Update page title based on current page
function updatePageTitle() {
    const currentPage = window.location.pathname.split('/').pop() || 'home.html';
    let title = 'Bistro Restaurant';
    
    // Set page specific titles
    if (currentPage === 'menu.html') {
        title = 'Menu - Bistro Restaurant';
    } else if (currentPage === 'about.html') {
        title = 'About Us - Bistro Restaurant';
    } else if (currentPage === 'contact.html') {
        title = 'Contact Us - Bistro Restaurant';
    } else if (currentPage === 'orders.html') {
        title = 'My Orders - Bistro Restaurant';
    } else if (currentPage === 'profile.html') {
        title = 'My Profile - Bistro Restaurant';
    } else if (currentPage === 'admin.html') {
        title = 'Admin Panel - Bistro Restaurant';
    }
    
    // Update the page title
    if (pageTitle) {
        pageTitle.textContent = title;
    }
}

// Format currency
function formatCurrency(amount) {
    return parseFloat(amount).toFixed(2);
}

// Format date
function formatDate(dateString) {
    const options = { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    };
    return new Date(dateString).toLocaleString('en-US', options);
}

// Get user data from session storage
function getCurrentUser() {
    return JSON.parse(sessionStorage.getItem('user'));
}

// Check if user is authenticated
function isAuthenticated() {
    return !!getCurrentUser();
}

// Check if user has admin or staff role
function hasAdminAccess() {
    const user = getCurrentUser();
    return user && (user.role === 'ADMIN' || user.role === 'STAFF');
}

// Generate user avatar element
function generateUserAvatar(username) {
    const container = document.createElement('div');
    container.className = 'user-avatar';
    
    // Use first letter of username for avatar
    const initial = username ? username.charAt(0).toUpperCase() : '';
    
    if (initial) {
        container.textContent = initial;
        
        // Generate a consistent color based on username
        const hash = username.split('').reduce((a, b) => {
            a = ((a << 5) - a) + b.charCodeAt(0);
            return a & a;
        }, 0);
        
        const hue = Math.abs(hash % 360);
        container.style.backgroundColor = `hsl(${hue}, 70%, 85%)`;
        container.style.color = `hsl(${hue}, 70%, 30%)`;
    } else {
        // Fallback to icon
        const icon = document.createElement('i');
        icon.className = 'fas fa-user';
        container.appendChild(icon);
    }
    
    return container;
}

// Update user avatar throughout the site
function updateUserAvatar() {
    const user = getCurrentUser();
    if (!user) return;
    
    // Update avatars in header and profile
    const avatarContainers = document.querySelectorAll('.user-avatar');
    
    avatarContainers.forEach(container => {
        // Clear existing content
        container.innerHTML = '';
        
        // Use first letter of username for avatar
        const initial = user.username ? user.username.charAt(0).toUpperCase() : '';
        
        if (initial) {
            container.textContent = initial;
            
            // Generate a consistent color based on username
            const hash = user.username.split('').reduce((a, b) => {
                a = ((a << 5) - a) + b.charCodeAt(0);
                return a & a;
            }, 0);
            
            const hue = Math.abs(hash % 360);
            container.style.backgroundColor = `hsl(${hue}, 70%, 85%)`;
            container.style.color = `hsl(${hue}, 70%, 30%)`;
            
            // Add role-based class for styling
            if (user.role) {
                container.classList.add(user.role.toLowerCase());
            }
        } else {
            // Fallback to icon
            const icon = document.createElement('i');
            icon.className = 'fas fa-user';
            container.appendChild(icon);
        }
        
        // Ensure all avatars link to profile
        if (container.tagName !== 'A') {
            // If not already a link, check if it's in a link
            const parent = container.parentElement;
            if (parent.tagName !== 'A') {
                // Wrap in a link
                const link = document.createElement('a');
                link.href = 'profile.html';
                link.className = container.className;
                link.innerHTML = container.innerHTML;
                
                // Copy any inline styles
                for (let i = 0; i < container.style.length; i++) {
                    const prop = container.style[i];
                    link.style[prop] = container.style[prop];
                }
                
                // Replace with link
                container.parentNode.replaceChild(link, container);
            }
        }
    });
    
    // Update profile name if on profile page
    const profileName = document.getElementById('profile-name');
    if (profileName) {
        const fullName = (user.firstName && user.lastName) ? 
            `${user.firstName} ${user.lastName}` : 
            user.username;
        profileName.textContent = fullName;
    }
    
    // Update profile role if on profile page
    const profileRole = document.getElementById('profile-role');
    if (profileRole && user.role) {
        profileRole.textContent = user.role.charAt(0) + user.role.slice(1).toLowerCase();
        profileRole.className = `user-role ${user.role.toLowerCase()}`;
    }
}
