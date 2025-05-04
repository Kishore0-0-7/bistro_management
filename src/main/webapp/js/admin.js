// Admin Dashboard script

// Add CSS for loading overlay
(function() {
    const style = document.createElement('style');
    style.textContent = `
        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        }
        
        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 5px solid #f3f3f3;
            border-top: 5px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    document.head.appendChild(style);
})();

// Global variables for DOM elements
let adminTabs, tabContents, dashboardTab, menuManagementTab, orderManagementTab, userManagementTab;

// Function to initialize DOM references
function initDomElements() {
    adminTabs = document.querySelectorAll('.tab-btn');
    tabContents = document.querySelectorAll('.tab-content');
    dashboardTab = document.getElementById('dashboard-tab');
    menuManagementTab = document.getElementById('menu-management-tab');
    orderManagementTab = document.getElementById('order-management-tab');
    userManagementTab = document.getElementById('user-management-tab');

    console.log('DOM elements initialized:');
    console.log('- adminTabs:', adminTabs ? adminTabs.length : 'none');
    console.log('- tabContents:', tabContents ? tabContents.length : 'none');
    console.log('- dashboardTab:', dashboardTab ? 'found' : 'not found');
    console.log('- menuManagementTab:', menuManagementTab ? 'found' : 'not found');
    console.log('- orderManagementTab:', orderManagementTab ? 'found' : 'not found');
    console.log('- userManagementTab:', userManagementTab ? 'found' : 'not found');
}

// Main initialization function
function initializeAdmin() {
    console.log('Admin dashboard initializing...');
    
    // Initialize DOM elements
    initDomElements();
    
    // Set up event listeners
    setupAdminEventListeners();
    
    // Check if user is admin
    if (checkAdminAccess()) {
        // Load admin dashboard
        loadAdminDashboard();
        
        // Set up form submission handlers
        setupFormSubmissionHandlers();
    }
}

// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check if jQuery is available
    if (typeof jQuery === 'undefined') {
        console.log('jQuery not detected, loading dynamically');
        
        // Create script element to load jQuery
        const script = document.createElement('script');
        script.src = 'https://code.jquery.com/jquery-3.6.0.min.js';
        
        // Set up onload handler
        script.onload = function() {
            console.log('jQuery loaded dynamically');
            initializeAdmin();
        };
        
        // Handle loading error
        script.onerror = function() {
            console.error('Failed to load jQuery, initializing without it');
            initializeAdmin();
        };
        
        // Add to document
        document.head.appendChild(script);
    } else {
        // jQuery already available
        console.log('jQuery already loaded');
        initializeAdmin();
    }
});

// Set up admin event listeners
function setupAdminEventListeners() {
    console.log('Setting up admin event listeners...');
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    // Tab switching
    if (adminTabs && adminTabs.length > 0) {
        console.log('Found admin tabs:', adminTabs.length);
        adminTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const tabId = tab.getAttribute('data-tab');
                console.log('Tab clicked:', tabId);
                switchTab(tabId);
            });
        });
    } else {
        console.error('Admin tabs not found or empty');
    }
    
    // Close modal buttons
    document.querySelectorAll('.close-modal').forEach(button => {
        button.addEventListener('click', () => {
            closeAllModals();
        });
    });
    
    // Close modal when clicking outside
    window.addEventListener('click', (e) => {
        document.querySelectorAll('.modal').forEach(modal => {
            if (e.target === modal) {
                closeAllModals();
            }
        });
    });
}

// Set up form submission handlers
function setupFormSubmissionHandlers() {
    console.log('Setting up form submission handlers...');
    
    // Edit user form
    const editUserForm = document.getElementById('edit-user-form');
    if (editUserForm) {
        editUserForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(editUserForm);
            updateUser(formData);
        });
    }
    
    // Add user form
    const addUserForm = document.getElementById('add-user-form');
    if (addUserForm) {
        addUserForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(addUserForm);
            addUser(formData);
        });
    }
    
    // Menu item form
    const menuItemForm = document.getElementById('menu-item-form');
    if (menuItemForm) {
        menuItemForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(menuItemForm);
            saveMenuItem(formData);
        });
    }
}

// Check if user has admin access
function checkAdminAccess() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (!user || (user.role !== 'ADMIN' && user.role !== 'STAFF')) {
        // Redirect to home if not admin or staff
        if (typeof showSection === 'function') {
            showSection('home');
        } else {
            // Fallback: hide admin section
            const adminSection = document.getElementById('admin');
            if (adminSection) {
                adminSection.classList.remove('active');
            }
            
            // Show home section
            const homeSection = document.getElementById('home');
            if (homeSection) {
                homeSection.classList.add('active');
            }
        }
        
        alert('You do not have permission to access the admin dashboard');
        return false;
    }
    
    return true;
}

// Load admin dashboard
function loadAdminDashboard() {
    console.log('Loading admin dashboard...');
    
    // Show dashboard tab by default
    switchTab('dashboard');
}

// Switch tab
function switchTab(tabId) {
    console.log('Switching to tab:', tabId);
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    // Remove active class from all tabs
    if (adminTabs && adminTabs.length > 0) {
        adminTabs.forEach(tab => {
            tab.classList.remove('active');
        });
    } else {
        console.error('Admin tabs not found or empty');
    }
    
    // Add active class to selected tab
    const selectedTab = document.querySelector(`.tab-btn[data-tab="${tabId}"]`);
    if (selectedTab) {
        selectedTab.classList.add('active');
    } else {
        console.error(`Tab button for ${tabId} not found`);
    }
    
    // Hide all tab contents
    if (tabContents && tabContents.length > 0) {
        tabContents.forEach(content => {
            content.classList.remove('active');
        });
    } else {
        console.error('Tab contents not found or empty');
    }
    
    // Show selected tab content
    const selectedContent = document.getElementById(`${tabId}-tab`);
    if (selectedContent) {
        selectedContent.classList.add('active');
    } else {
        console.error(`Tab content for ${tabId} not found`);
    }
    
    // Load content based on tab
    switch (tabId) {
        case 'dashboard':
            loadDashboardContent();
            break;
        case 'menu-management':
            loadMenuManagementContent();
            break;
        case 'order-management':
            loadOrderManagementContent();
            break;
        case 'user-management':
            loadUserManagementContent();
            break;
        default:
            console.error(`Unknown tab: ${tabId}`);
    }
}

// Load dashboard content
function loadDashboardContent() {
    console.log('Loading dashboard content...');
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    if (!dashboardTab) {
        console.error('Dashboard tab element not found');
        return;
    }
    
    dashboardTab.innerHTML = '<div class="loading">Loading dashboard...</div>';
    
    fetch('api/admin/dashboard')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch dashboard data: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Dashboard data from server:', data);
            displayDashboardContent(data);
        })
        .catch(error => {
            console.error('Error loading dashboard:', error);
            
            if (!dashboardTab) return; // Check if element still exists
            
            // Show error message with retry button
            dashboardTab.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Dashboard</h3>
                    <p>${error.message}</p>
                    <button class="btn-primary retry-btn">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            const retryBtn = dashboardTab.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', loadDashboardContent);
            }
        });
}

// Display dashboard content
function displayDashboardContent(data) {
    console.log('Displaying dashboard content:', data);
    
    if (!dashboardTab) {
        console.error('Dashboard tab element not found');
        return;
    }
    
    // Ensure data has all required properties with defaults
    const safeData = {
        totalOrders: data?.totalOrders || 0,
        totalRevenue: data?.totalRevenue || 0,
        pendingOrders: data?.pendingOrders || 0,
        totalUsers: data?.totalUsers || 0,
        orderStatusCounts: data?.orderStatusCounts || {
            PENDING: 0,
            PREPARING: 0,
            READY: 0,
            DELIVERED: 0,
            CANCELLED: 0
        },
        recentOrders: data?.recentOrders || []
    };
    
    // Calculate total orders for percentage calculation
    const totalOrdersForStatus = Object.values(safeData.orderStatusCounts).reduce((a, b) => a + b, 0) || 1; // Avoid division by zero
    
    // Parse and format totalRevenue safely
    let totalRevenue = 0;
    try {
        totalRevenue = typeof safeData.totalRevenue === 'number' 
            ? safeData.totalRevenue 
            : parseFloat(safeData.totalRevenue || 0);
        
        if (isNaN(totalRevenue)) totalRevenue = 0;
    } catch (e) {
        console.error('Error parsing totalRevenue', e);
    }
    
    dashboardTab.innerHTML = `
        <div class="dashboard-stats">
            <div class="stat-card">
                <div class="stat-icon">
                    <i class="fas fa-shopping-cart"></i>
                </div>
                <div class="stat-content">
                    <h4>Total Orders</h4>
                    <p>${safeData.totalOrders}</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">
                    <i class="fas fa-dollar-sign"></i>
                </div>
                <div class="stat-content">
                    <h4>Total Revenue</h4>
                    <p>$${totalRevenue.toFixed(2)}</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">
                    <i class="fas fa-clock"></i>
                </div>
                <div class="stat-content">
                    <h4>Pending Orders</h4>
                    <p>${safeData.pendingOrders}</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">
                    <i class="fas fa-users"></i>
                </div>
                <div class="stat-content">
                    <h4>Total Users</h4>
                    <p>${safeData.totalUsers}</p>
                </div>
            </div>
        </div>
        
        <div class="dashboard-sections">
            <div class="dashboard-section">
                <h3>Order Status</h3>
                <div class="order-status-chart">
                    <div class="status-bar">
                        <div class="status-segment pending" style="width: ${(safeData.orderStatusCounts.PENDING / totalOrdersForStatus * 100) || 0}%">
                            <span>Pending (${safeData.orderStatusCounts.PENDING})</span>
                        </div>
                        <div class="status-segment preparing" style="width: ${(safeData.orderStatusCounts.PREPARING / totalOrdersForStatus * 100) || 0}%">
                            <span>Preparing (${safeData.orderStatusCounts.PREPARING})</span>
                        </div>
                        <div class="status-segment ready" style="width: ${(safeData.orderStatusCounts.READY / totalOrdersForStatus * 100) || 0}%">
                            <span>Ready (${safeData.orderStatusCounts.READY})</span>
                        </div>
                        <div class="status-segment delivered" style="width: ${(safeData.orderStatusCounts.DELIVERED / totalOrdersForStatus * 100) || 0}%">
                            <span>Delivered (${safeData.orderStatusCounts.DELIVERED})</span>
                        </div>
                        <div class="status-segment cancelled" style="width: ${(safeData.orderStatusCounts.CANCELLED / totalOrdersForStatus * 100) || 0}%">
                            <span>Cancelled (${safeData.orderStatusCounts.CANCELLED})</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="dashboard-section">
                <h3>Recent Orders</h3>
                <div class="recent-orders">
                    ${getRecentOrdersHtml(safeData.recentOrders)}
                </div>
            </div>
        </div>
    `;
    
    // Add event listeners to view order buttons
    const viewOrderButtons = dashboardTab.querySelectorAll('.view-order-btn');
    if (viewOrderButtons && viewOrderButtons.length > 0) {
        viewOrderButtons.forEach(button => {
            button.addEventListener('click', () => {
                const orderId = button.getAttribute('data-id');
                viewOrderDetails(orderId);
            });
        });
    }
}

// Get recent orders HTML
function getRecentOrdersHtml(orders) {
    if (!orders || orders.length === 0) {
        return '<p>No recent orders</p>';
    }
    
    let html = '<div class="recent-orders-list">';
    
    orders.forEach(order => {
        // Format items
        let itemsText = '';
        if (order.items && order.items.length > 0) {
            const itemsList = order.items.map(item => `${item.menuItemName} x${item.quantity}`);
            itemsText = itemsList.join(', ');
        }
        
        // Safely parse the order total amount
        let totalAmount = 0;
        try {
            totalAmount = typeof order.totalAmount === 'number' 
                ? order.totalAmount 
                : parseFloat(order.totalAmount || 0);
            
            if (isNaN(totalAmount)) totalAmount = 0;
        } catch (e) {
            console.error('Error parsing order totalAmount in recent orders', e);
        }
        
        html += `
            <div class="recent-order-item">
                <div class="recent-order-header">
                    <span class="order-id">#${order.id}</span>
                    <span class="order-status ${order.status.toLowerCase()}">${order.status}</span>
                </div>
                <div class="recent-order-details">
                    <p><strong>Customer:</strong> ${order.userName || 'User #' + order.userId}</p>
                    <p><strong>Date:</strong> ${formatDate(order.orderDate)}</p>
                    <p><strong>Items:</strong> ${itemsText}</p>
                    <p><strong>Total:</strong> $${totalAmount.toFixed(2)}</p>
                </div>
                <div class="recent-order-actions">
                    <button class="btn-primary view-order-btn" data-id="${order.id}">View Details</button>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    
    return html;
}

// Load user management content
function loadUserManagementContent() {
    if (!userManagementTab) return;
    
    userManagementTab.innerHTML = '<div class="loading">Loading user management...</div>';
    
    fetch('api/admin/users')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch users');
            }
            return response.json();
        })
        .then(users => {
            console.log('User data from server:', users);
            displayUserManagementContent(users);
        })
        .catch(error => {
            console.error('Error loading users:', error);
            
            // Show error message instead of using sample data
            userManagementTab.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Users</h3>
                    <p>${error.message}</p>
                    <p>Please try again later or contact support.</p>
                    <button class="btn-primary retry-btn">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            const retryBtn = userManagementTab.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', loadUserManagementContent);
            }
        });
}

// Helper function to safely render a table 
function renderAdminTable(container, tableData, columns, rowCallback) {
    if (!container) {
        console.error('Container element not found for table rendering');
        return;
    }
    
    // Create table element structure
    const table = document.createElement('table');
    table.className = 'admin-table';
    
    // Create table header
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    
    // Add header columns
    columns.forEach(column => {
        const th = document.createElement('th');
        th.textContent = column.label;
        headerRow.appendChild(th);
    });
    
    thead.appendChild(headerRow);
    table.appendChild(thead);
    
    // Create table body
    const tbody = document.createElement('tbody');
    
    // Add table rows
    if (!tableData || tableData.length === 0) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');
        td.colSpan = columns.length;
        td.textContent = 'No items found';
        td.style.textAlign = 'center';
        tr.appendChild(td);
        tbody.appendChild(tr);
    } else {
        tableData.forEach(item => {
            const tr = document.createElement('tr');
            
            // Set custom attributes from callback
            if (rowCallback) {
                const attrs = rowCallback(item);
                if (attrs) {
                    Object.entries(attrs).forEach(([key, value]) => {
                        tr.setAttribute(key, value);
                    });
                }
            }
            
            // Add columns to the row
            columns.forEach(column => {
                const td = document.createElement('td');
                
                // Use renderer function if provided, otherwise use direct value
                if (column.renderer) {
                    td.innerHTML = column.renderer(item);
                } else if (column.field) {
                    td.textContent = item[column.field] || '';
                }
                
                tr.appendChild(td);
            });
            
            tbody.appendChild(tr);
        });
    }
    
    table.appendChild(tbody);
    
    // Clear container and append the table
    container.innerHTML = '';
    container.appendChild(table);
    
    // Return the created table element for further manipulation
    return table;
}

// Display user management content with the safer table rendering
function displayUserManagementContent(users) {
    console.log('Displaying user management content:', users);
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    if (!userManagementTab) {
        console.error('User management tab element not found');
        return;
    }
    
    // Create the container structure
    userManagementTab.innerHTML = `
        <div class="admin-header">
            <h3>User Management</h3>
            <button id="add-user-btn" class="btn-primary">Add New User</button>
        </div>
        
        <div class="admin-filters">
            <div class="search-box">
                <input type="text" id="user-search" placeholder="Search users...">
                <button id="user-search-btn"><i class="fas fa-search"></i></button>
            </div>
            
            <div class="filter-options">
                <select id="user-role-filter">
                    <option value="all">All Roles</option>
                    <option value="ADMIN">Admin</option>
                    <option value="STAFF">Staff</option>
                    <option value="CUSTOMER">Customer</option>
                </select>
            </div>
        </div>
        
        <div class="users-list"></div>
    `;
    
    // Define columns for the users table
    const userColumns = [
        { label: 'ID', field: 'id', renderer: user => `#${user.id}` },
        { label: 'Username', field: 'username' },
        { label: 'Email', field: 'email' },
        { label: 'Role', renderer: user => `<span class="user-role ${user.role.toLowerCase()}">${user.role}</span>` },
        { label: 'Created', renderer: user => formatDate(user.createdAt) },
        { label: 'Actions', renderer: user => `
            <button class="btn-primary view-user-btn" data-id="${user.id}">View</button>
            <button class="btn-secondary edit-user-btn" data-id="${user.id}">Edit</button>
            ${user.role !== 'ADMIN' ? `<button class="btn-danger delete-user-btn" data-id="${user.id}" data-username="${user.username}">Delete</button>` : ''}
        `}
    ];
    
    // Render the table safely
    renderAdminTable(
        userManagementTab.querySelector('.users-list'),
        users,
        userColumns
    );
    
    // Add event listeners
    const addUserBtn = document.getElementById('add-user-btn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', showAddUserForm);
    }
    
    const userSearchBtn = document.getElementById('user-search-btn');
    const userSearchInput = document.getElementById('user-search');
    if (userSearchBtn && userSearchInput) {
        userSearchBtn.addEventListener('click', () => {
            searchUsers(userSearchInput.value);
        });
        
        userSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchUsers(userSearchInput.value);
            }
        });
    }
    
    const userRoleFilter = document.getElementById('user-role-filter');
    if (userRoleFilter) {
        userRoleFilter.addEventListener('change', () => {
            filterUsersByRole(userRoleFilter.value);
        });
    }
    
    // Add event listeners to user action buttons
    const usersList = userManagementTab.querySelector('.users-list');
    if (usersList) {
        usersList.addEventListener('click', function(e) {
            // View user button
            if (e.target.classList.contains('view-user-btn')) {
                const userId = e.target.getAttribute('data-id');
            viewUserDetails(userId);
            }
            
            // Edit user button
            if (e.target.classList.contains('edit-user-btn')) {
                const userId = e.target.getAttribute('data-id');
            editUser(userId);
            }
            
            // Delete user button
            if (e.target.classList.contains('delete-user-btn')) {
                const userId = e.target.getAttribute('data-id');
                const username = e.target.getAttribute('data-username');
            if (confirm(`Are you sure you want to delete user ${username}?`)) {
                deleteUser(userId);
            }
            }
    });
}
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}

// View user details
function viewUserDetails(userId) {
    fetch(`api/admin/users/${userId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user details');
            }
            return response.json();
        })
        .then(user => {
            displayUserDetails(user);
            showModal('user-details-modal');
        })
        .catch(error => {
            console.error(`Error loading user details for user ${userId}:`, error);
            
            // Show error message with retry button
            const userDetailsContainer = document.getElementById('user-details');
            if (userDetailsContainer) {
                userDetailsContainer.innerHTML = `
                    <div class="error-message">
                        <i class="fas fa-exclamation-circle"></i>
                        <h3>Error Loading User Details</h3>
                        <p>${error.message}</p>
                        <button class="btn-primary retry-btn">Retry</button>
                    </div>
                `;
                
                // Add retry button functionality
                const retryBtn = userDetailsContainer.querySelector('.retry-btn');
                if (retryBtn) {
                    retryBtn.addEventListener('click', () => viewUserDetails(userId));
                }
            }
        });
}

// Display user details in modal
function displayUserDetails(user) {
    const userDetailsContainer = document.getElementById('user-details');
    
    userDetailsContainer.innerHTML = `
        <div class="user-detail-header">
            <h3>${user.firstName} ${user.lastName}</h3>
            <p class="user-role ${user.role.toLowerCase()}">${user.role}</p>
        </div>
        <div class="user-detail-info">
            <p><strong>Username:</strong> ${user.username}</p>
            <p><strong>Email:</strong> ${user.email}</p>
            <p><strong>Phone:</strong> ${user.phone || 'Not provided'}</p>
            <p><strong>Address:</strong> ${user.address || 'Not provided'}</p>
            <p><strong>Joined:</strong> ${formatDate(user.createdAt)}</p>
        </div>
        <div class="user-detail-actions">
            <button class="btn-primary edit-user-btn" data-id="${user.id}">Edit User</button>
            ${user.role !== 'ADMIN' ? `<button class="btn-danger delete-user-btn" data-id="${user.id}">Delete User</button>` : ''}
        </div>
    `;
    
    // Add event listeners
    userDetailsContainer.querySelector('.edit-user-btn').addEventListener('click', () => {
        showEditUserForm(user);
    });
    
    const deleteBtn = userDetailsContainer.querySelector('.delete-user-btn');
    if (deleteBtn) {
        deleteBtn.addEventListener('click', () => {
            if (confirm(`Are you sure you want to delete user ${user.username}?`)) {
                deleteUser(user.id);
            }
        });
    }
}

// Show edit user form
function showEditUserForm(user) {
    closeAllModals();
    showModal('edit-user-modal');
    
    document.getElementById('edit-user-id').value = user.id;
    document.getElementById('edit-user-username').value = user.username;
    document.getElementById('edit-user-email').value = user.email;
    document.getElementById('edit-user-first-name').value = user.firstName || '';
    document.getElementById('edit-user-last-name').value = user.lastName || '';
    document.getElementById('edit-user-role').value = user.role;
    document.getElementById('edit-user-phone').value = user.phone || '';
    document.getElementById('edit-user-address').value = user.address || '';
    
    // Clear password fields if they exist
    const passwordField = document.getElementById('edit-user-password');
    if (passwordField) passwordField.value = '';
    
    const confirmPasswordField = document.getElementById('edit-user-confirm-password');
    if (confirmPasswordField) confirmPasswordField.value = '';
}

// Update user
function updateUser(formData) {
    const userId = formData.get('id');
    
    // Check if password fields are provided and match
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    if (password && confirmPassword && password !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }
    
    const userData = {
        username: formData.get('username'),
        email: formData.get('email'),
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        role: formData.get('role'),
        phone: formData.get('phone'),
        address: formData.get('address')
    };
    
    // Update user profile
    fetch(`api/admin/users/${userId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(userData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update user');
        }
        return response.json();
    })
    .then(data => {
        // If password was provided, also update password
        if (password && password.trim() !== '') {
            return changeUserPassword(userId, password);
        }
        return data;
    })
    .then(data => {
        alert('User updated successfully');
        closeAllModals();
        loadUserManagementContent();
    })
    .catch(error => {
        alert(`Error updating user: ${error.message}`);
    });
}

// Change user password (admin function)
function changeUserPassword(userId, newPassword) {
    return fetch(`api/admin/users/${userId}/password`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            newPassword: newPassword
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update password');
        }
        return response.json();
    });
}

// Delete user
function deleteUser(userId) {
    fetch(`api/admin/users/${userId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete user');
        }
        return response.json();
    })
    .then(data => {
        alert('User deleted successfully');
        closeAllModals();
        loadUserManagementContent();
    })
    .catch(error => {
        alert(`Error deleting user: ${error.message}`);
    });
}

// Edit user
function editUser(userId) {
    fetch(`api/admin/users/${userId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch user details');
            }
            return response.json();
        })
        .then(user => {
            console.log('User details from server:', user);
            showEditUserForm(user);
        })
        .catch(error => {
            console.error(`Error loading user details for user ${userId}:`, error);
            alert(`Error loading user details: ${error.message}`);
        });
}

// Load menu management content
function loadMenuManagementContent() {
    if (!menuManagementTab) return;
    
    menuManagementTab.innerHTML = '<div class="loading">Loading menu management...</div>';
    
    fetch('api/menu')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch menu items');
            }
            return response.json();
        })
        .then(items => {
            console.log('Menu data from server:', items);
            displayMenuManagementContent(items);
        })
        .catch(error => {
            console.error('Error loading menu items:', error);
            
            // Show error message instead of using sample data
            menuManagementTab.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Menu Items</h3>
                    <p>${error.message}</p>
                    <p>Please try again later or contact support.</p>
                    <button class="btn-primary retry-btn">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            const retryBtn = menuManagementTab.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', loadMenuManagementContent);
            }
        });
}

// Display menu management content
function displayMenuManagementContent(items) {
    console.log('Displaying menu management content:', items);
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    if (!menuManagementTab) {
        console.error('Menu management tab element not found');
        return;
    }
    
    // Create the container structure
    menuManagementTab.innerHTML = `
        <div class="admin-header">
            <h3>Menu Management</h3>
            <button id="add-menu-item-btn" class="btn-primary">Add New Menu Item</button>
        </div>
        
        <div class="admin-filters">
            <div class="search-box">
                <input type="text" id="menu-search" placeholder="Search menu items...">
                <button id="menu-search-btn"><i class="fas fa-search"></i></button>
            </div>
            
            <div class="filter-options">
                <select id="menu-category-filter">
                    <option value="all">All Categories</option>
                    <option value="Pizza">Pizza</option>
                    <option value="Pasta">Pasta</option>
                    <option value="Salad">Salad</option>
                    <option value="Main Course">Main Course</option>
                    <option value="Dessert">Dessert</option>
                    <option value="Beverage">Beverage</option>
                </select>
                
                <select id="menu-availability-filter">
                    <option value="all">All Items</option>
                    <option value="available">Available Only</option>
                    <option value="unavailable">Unavailable Only</option>
                </select>
            </div>
        </div>
        
        <div class="menu-items-list"></div>
    `;
    
    // Define columns for the menu items table
    const menuColumns = [
        { label: 'ID', renderer: item => `#${item.id}` },
        { label: 'Image', renderer: item => `<img src="${item.imageUrl || 'images/menu/default.jpg'}" alt="${item.name}" class="menu-item-thumbnail">` },
        { label: 'Name', field: 'name' },
        { label: 'Category', field: 'category' },
        { label: 'Price', renderer: item => `$${parseFloat(item.price).toFixed(2)}` },
        { label: 'Available', renderer: item => `<span class="status-badge ${item.available ? 'available' : 'unavailable'}">${item.available ? 'Yes' : 'No'}</span>` },
        { label: 'Featured', renderer: item => `<span class="status-badge ${item.featured ? 'featured' : 'not-featured'}">${item.featured ? 'Yes' : 'No'}</span>` },
        { label: 'Actions', renderer: item => `
            <button class="btn-primary edit-menu-item-btn" data-id="${item.id}">Edit</button>
            <button class="btn-danger delete-menu-item-btn" data-id="${item.id}" data-name="${item.name}">Delete</button>
            <button class="btn-secondary toggle-availability-btn" data-id="${item.id}" data-available="${item.available}">
                ${item.available ? 'Disable' : 'Enable'}
            </button>
            <button class="btn-secondary toggle-featured-btn" data-id="${item.id}" data-featured="${item.featured}">
                ${item.featured ? 'Unfeature' : 'Feature'}
            </button>
        ` }
    ];
    
    // Row callback to add custom attributes
    const rowCallback = item => ({
        'data-category': item.category,
        'data-available': item.available
    });
    
    // Render the table safely
    renderAdminTable(
        menuManagementTab.querySelector('.menu-items-list'),
        items,
        menuColumns,
        rowCallback
    );
    
    // Add event listeners
    const addMenuItemBtn = document.getElementById('add-menu-item-btn');
    if (addMenuItemBtn) {
        addMenuItemBtn.addEventListener('click', showAddMenuItemForm);
    }
    
    const menuSearchBtn = document.getElementById('menu-search-btn');
    const menuSearchInput = document.getElementById('menu-search');
    if (menuSearchBtn && menuSearchInput) {
        menuSearchBtn.addEventListener('click', () => {
            searchMenuItems(menuSearchInput.value);
        });
        
        menuSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchMenuItems(menuSearchInput.value);
            }
        });
    }
    
    const menuCategoryFilter = document.getElementById('menu-category-filter');
    if (menuCategoryFilter) {
        menuCategoryFilter.addEventListener('change', () => {
            filterMenuItemsByCategory(menuCategoryFilter.value);
        });
    }
    
    const menuAvailabilityFilter = document.getElementById('menu-availability-filter');
    if (menuAvailabilityFilter) {
        menuAvailabilityFilter.addEventListener('change', () => {
            filterMenuItemsByAvailability(menuAvailabilityFilter.value);
        });
    }
    
    // Add event listeners to menu item action buttons using event delegation
    const menuItemsList = menuManagementTab.querySelector('.menu-items-list');
    if (menuItemsList) {
        menuItemsList.addEventListener('click', function(e) {
            // Edit button
            if (e.target.classList.contains('edit-menu-item-btn')) {
                const itemId = e.target.getAttribute('data-id');
            editMenuItem(itemId);
            }
            
            // Delete button
            if (e.target.classList.contains('delete-menu-item-btn')) {
                const itemId = e.target.getAttribute('data-id');
                const itemName = e.target.getAttribute('data-name');
            if (confirm(`Are you sure you want to delete menu item "${itemName}"?`)) {
                deleteMenuItem(itemId);
            }
            }
            
            // Toggle availability button
            if (e.target.classList.contains('toggle-availability-btn')) {
                const itemId = e.target.getAttribute('data-id');
                const currentAvailability = e.target.getAttribute('data-available') === 'true';
            toggleMenuItemAvailability(itemId, !currentAvailability);
            }
            
            // Toggle featured button
            if (e.target.classList.contains('toggle-featured-btn')) {
                const itemId = e.target.getAttribute('data-id');
                const currentFeatured = e.target.getAttribute('data-featured') === 'true';
            toggleMenuItemFeatured(itemId, !currentFeatured);
            }
    });
    }
}

// Get menu items list HTML
function getMenuItemsListHtml(items) {
    if (!items || items.length === 0) {
        return '<p>No menu items found</p>';
    }
    
    let html = '<table class="admin-table"><thead><tr><th>ID</th><th>Image</th><th>Name</th><th>Category</th><th>Price</th><th>Available</th><th>Featured</th><th>Actions</th></tr></thead><tbody>';
    
    items.forEach(item => {
        html += `
            <tr data-category="${item.category}" data-available="${item.available}">
                <td>#${item.id}</td>
                <td><img src="${item.imageUrl || 'images/menu/default.jpg'}" alt="${item.name}" class="menu-item-thumbnail"></td>
                <td>${item.name}</td>
                <td>${item.category}</td>
                <td>$${item.price.toFixed(2)}</td>
                <td><span class="status-badge ${item.available ? 'available' : 'unavailable'}">${item.available ? 'Yes' : 'No'}</span></td>
                <td><span class="status-badge ${item.featured ? 'featured' : 'not-featured'}">${item.featured ? 'Yes' : 'No'}</span></td>
                <td>
                    <button class="btn-primary edit-menu-item-btn" data-id="${item.id}">Edit</button>
                    <button class="btn-danger delete-menu-item-btn" data-id="${item.id}" data-name="${item.name}">Delete</button>
                    <button class="btn-secondary toggle-availability-btn" data-id="${item.id}" data-available="${item.available}">
                        ${item.available ? 'Disable' : 'Enable'}
                    </button>
                    <button class="btn-secondary toggle-featured-btn" data-id="${item.id}" data-featured="${item.featured}">
                        ${item.featured ? 'Unfeature' : 'Feature'}
                    </button>
                </td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    
    return html;
}

// Delete menu item
function deleteMenuItem(itemId) {
    fetch(`api/menu/${itemId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to delete menu item');
        }
        return response.json();
    })
    .then(data => {
        alert('Menu item deleted successfully');
        loadMenuManagementContent();
    })
    .catch(error => {
        console.error(`Error deleting menu item ${itemId}:`, error);
        alert(`Error deleting menu item: ${error.message}`);
    });
}

// Toggle menu item availability
function toggleMenuItemAvailability(itemId, available) {
    // Extract the ID correctly from the table
    // Check if ID starts with '#' and remove it
    if (typeof itemId === 'string' && itemId.startsWith('#')) {
        itemId = itemId.substring(1);
    }
    
    console.log(`Toggling availability for item ${itemId} to: ${available}`);
    
    // Show loading indicator
    const loadingModal = document.createElement('div');
    loadingModal.className = 'loading-overlay';
    loadingModal.innerHTML = '<div class="loading-spinner"></div>';
    document.body.appendChild(loadingModal);
    
    // Make the AJAX request
    $.ajax({
        url: `api/menu/items/${itemId}/availability`,
        type: 'PUT',
        contentType: 'application/json',
        // Empty body as the endpoint likely doesn't require a body
        data: JSON.stringify({}),
        success: function(response) {
            document.body.removeChild(loadingModal);
            console.log('Availability updated successfully', response);
            alert(`Menu item ${available ? 'enabled' : 'disabled'} successfully`);
            loadMenuManagementContent(); // Refresh the menu items list
        },
        error: function(xhr, status, error) {
            document.body.removeChild(loadingModal);
            console.error('Error updating availability:', error);
            
            // Log the exact item ID for debugging
            console.log('Attempted to toggle availability with Item ID:', itemId);
            console.log('Item ID type:', typeof itemId);
            
            // Try to parse error response
            if (xhr.responseText) {
                try {
                    const errorResponse = JSON.parse(xhr.responseText);
                    console.error('Error details:', errorResponse);
                    alert(`Error: ${errorResponse.error || 'Failed to update availability'}`);
                } catch (e) {
                    console.error('Error parsing error response:', e);
                    console.error('Response text:', xhr.responseText);
                    alert(`Error: Failed to update availability. ${xhr.status}: ${xhr.statusText}`);
                }
            } else {
                alert(`Error: Failed to update availability. ${xhr.status}: ${xhr.statusText}`);
            }
        }
    });
}

// Toggle menu item featured status
function toggleMenuItemFeatured(itemId, featured) {
    // Extract the ID correctly from the table 
    // Check if ID starts with '#' and remove it
    if (typeof itemId === 'string' && itemId.startsWith('#')) {
        itemId = itemId.substring(1);
    }
    
    console.log(`Toggling feature status for item ${itemId} to: ${featured}`);
    
    // Show loading indicator
    const loadingModal = document.createElement('div');
    loadingModal.className = 'loading-overlay';
    loadingModal.innerHTML = '<div class="loading-spinner"></div>';
    document.body.appendChild(loadingModal);
    
    // Make the AJAX request
    $.ajax({
        // Looking at screenshot, the URL structure needs to be:
        url: `api/menu/items/${itemId}/feature`,
        type: 'PUT',
        contentType: 'application/json',
        // Empty body as the endpoint likely doesn't require a body
        data: JSON.stringify({}),
        success: function(response) {
            document.body.removeChild(loadingModal);
            console.log('Featured status updated successfully', response);
            alert(`Menu item ${featured ? 'featured' : 'unfeatured'} successfully`);
            loadMenuManagementContent(); // Refresh the menu items list
        },
        error: function(xhr, status, error) {
            document.body.removeChild(loadingModal);
            console.error('Error updating featured status:', error);
            
            // Try another format as fallback
            $.ajax({
                url: `api/menu/items/${itemId}/featured`,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({}),
                success: function(response) {
                    document.body.removeChild(loadingModal);
                    console.log('Featured status updated successfully with alternative URL', response);
        alert(`Menu item ${featured ? 'featured' : 'unfeatured'} successfully`);
                    loadMenuManagementContent(); // Refresh the menu items list
                },
                error: function(innerXhr, innerStatus, innerError) {
                    console.error('Error updating featured status with both URL formats:', error);
                    
                    // Log the exact item ID for debugging
                    console.log('Attempted to toggle feature status with Item ID:', itemId);
                    console.log('Item ID type:', typeof itemId);
                    
                    // Try to parse error response
                    if (xhr.responseText) {
                        try {
                            const errorResponse = JSON.parse(xhr.responseText);
                            console.error('Error details:', errorResponse);
                            alert(`Error: ${errorResponse.error || 'Failed to update featured status'}`);
                        } catch (e) {
                            console.error('Error parsing error response:', e);
                            console.error('Response text:', xhr.responseText);
                            alert(`Error: Failed to update featured status. ${xhr.status}: ${xhr.statusText}`);
                        }
                    } else {
                        alert(`Error: Failed to update featured status. ${xhr.status}: ${xhr.statusText}`);
                    }
                }
            });
        }
    });
}

// Filter menu items by category
function filterMenuItemsByCategory(category) {
    const rows = menuManagementTab.querySelectorAll('.menu-items-list table tbody tr');
    
    rows.forEach(row => {
        if (category === 'all' || row.getAttribute('data-category') === category) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Filter menu items by availability
function filterMenuItemsByAvailability(availability) {
    const rows = menuManagementTab.querySelectorAll('.menu-items-list table tbody tr');
    
    rows.forEach(row => {
        const isAvailable = row.getAttribute('data-available') === 'true';
        
        if (availability === 'all' || 
            (availability === 'available' && isAvailable) || 
            (availability === 'unavailable' && !isAvailable)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Search menu items
function searchMenuItems(query) {
    if (!query) {
        // If query is empty, show all items
        const rows = menuManagementTab.querySelectorAll('.menu-items-list table tbody tr');
        rows.forEach(row => {
            row.style.display = '';
        });
        return;
    }
    
    query = query.toLowerCase();
    const rows = menuManagementTab.querySelectorAll('.menu-items-list table tbody tr');
    
    rows.forEach(row => {
        const name = row.querySelector('td:nth-child(3)').textContent.toLowerCase();
        const category = row.querySelector('td:nth-child(4)').textContent.toLowerCase();
        
        if (name.includes(query) || category.includes(query)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Load order management content
function loadOrderManagementContent() {
    console.log('Loading order management content...');
    
    if (!orderManagementTab) {
        console.error('Order management tab element not found');
        return;
    }
    
    orderManagementTab.innerHTML = '<div class="loading">Loading order management...</div>';
    
    // Try to fetch orders from the API
    fetch('api/orders')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch orders');
            }
            return response.json();
        })
        .then(orders => {
            console.log('Orders data from server:', orders);
            displayOrderManagementContent(orders);
        })
        .catch(error => {
            console.error('Error loading orders:', error);
            
            // Show error message instead of using sample data
            orderManagementTab.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Orders</h3>
                    <p>${error.message}</p>
                    <p>Please try again later or contact support.</p>
                    <button class="btn-primary retry-btn">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            const retryBtn = orderManagementTab.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', loadOrderManagementContent);
            }
        });
}

// Display order management content
function displayOrderManagementContent(orders) {
    console.log('Displaying order management content:', orders);
    
    // Re-initialize DOM elements to ensure they're available
    initDomElements();
    
    if (!orderManagementTab) {
        console.error('Order management tab element not found');
        return;
    }
    
    // Create the container structure
    orderManagementTab.innerHTML = `
        <div class="admin-header">
            <h3>Order Management</h3>
        </div>
        
        <div class="admin-filters">
            <div class="filter-buttons">
                <button class="filter-btn active" data-status="all">All Orders</button>
                <button class="filter-btn" data-status="PENDING">Pending</button>
                <button class="filter-btn" data-status="PREPARING">Preparing</button>
                <button class="filter-btn" data-status="READY">Ready</button>
                <button class="filter-btn" data-status="DELIVERED">Delivered</button>
                <button class="filter-btn" data-status="CANCELLED">Cancelled</button>
            </div>
            
            <div class="search-box">
                <input type="text" id="order-search" placeholder="Search orders...">
                <button id="order-search-btn"><i class="fas fa-search"></i></button>
            </div>
        </div>
        
        <div class="orders-list"></div>
    `;
    
    // Define columns for the orders table
    const orderColumns = [
        { label: 'ID', renderer: order => `#${order.id}` },
        { label: 'Customer', renderer: order => order.userName || 'User #' + order.userId },
        { label: 'Date', renderer: order => formatDate(order.orderDate) },
        { label: 'Items', renderer: order => getOrderItemsCount(order) },
        { label: 'Total', renderer: order => {
            let totalAmount = 0;
            try {
                totalAmount = typeof order.totalAmount === 'number' 
                    ? order.totalAmount 
                    : parseFloat(order.totalAmount || 0);
                
                if (isNaN(totalAmount)) totalAmount = 0;
            } catch (e) {
                console.error('Error parsing totalAmount', e);
                totalAmount = 0;
            }
            return `$${totalAmount.toFixed(2)}`;
        }},
        { label: 'Status', renderer: order => `<span class="order-status ${order.status.toLowerCase()}">${order.status}</span>` },
        { label: 'Actions', renderer: order => `
            <button class="btn-primary view-order-btn" data-id="${order.id}">View</button>
            <button class="btn-secondary update-status-btn" data-id="${order.id}" data-status="${order.status}">Update Status</button>
        `}
    ];
    
    // Row callback to add custom attributes
    const rowCallback = order => ({
        'data-status': order.status,
        'data-id': order.id
    });
    
    // Render the table safely
    renderAdminTable(
        orderManagementTab.querySelector('.orders-list'),
        orders,
        orderColumns,
        rowCallback
    );
    
    // Add event listeners to filter buttons
    const filterButtons = orderManagementTab.querySelectorAll('.filter-btn');
    if (filterButtons && filterButtons.length > 0) {
        filterButtons.forEach(button => {
            button.addEventListener('click', () => {
                // Remove active class from all filter buttons
                filterButtons.forEach(btn => {
                    btn.classList.remove('active');
                });
                
                // Add active class to clicked button
                button.classList.add('active');
                
                // Filter orders
                const status = button.getAttribute('data-status');
                filterOrdersByStatus(status);
            });
        });
    }
    
    // Add event listeners to search
    const orderSearchBtn = document.getElementById('order-search-btn');
    const orderSearchInput = document.getElementById('order-search');
    if (orderSearchBtn && orderSearchInput) {
        orderSearchBtn.addEventListener('click', () => {
            searchOrders(orderSearchInput.value);
        });
        
        orderSearchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchOrders(orderSearchInput.value);
            }
        });
    }
    
    // Add event delegation for order buttons
    const ordersList = orderManagementTab.querySelector('.orders-list');
    if (ordersList) {
        ordersList.addEventListener('click', function(e) {
            // View order button
            if (e.target.classList.contains('view-order-btn')) {
                const orderId = e.target.getAttribute('data-id');
                viewOrderDetails(orderId);
            }
            
            // Update status button
            if (e.target.classList.contains('update-status-btn')) {
                const orderId = e.target.getAttribute('data-id');
                const currentStatus = e.target.getAttribute('data-status');
                updateOrderStatus(orderId, currentStatus);
            }
            });
    }
}

// Get orders list HTML
function getOrdersListHtml(orders) {
    if (!orders || orders.length === 0) {
        return '<p>No orders found</p>';
    }
    
    let html = '<table class="admin-table"><thead><tr><th>ID</th><th>Customer</th><th>Date</th><th>Items</th><th>Total</th><th>Status</th><th>Actions</th></tr></thead><tbody>';
    
    orders.forEach(order => {
        // Ensure totalAmount is a number and properly formatted
        let totalAmount = 0;
        try {
            totalAmount = typeof order.totalAmount === 'number' 
                ? order.totalAmount 
                : parseFloat(order.totalAmount || 0);
            
            if (isNaN(totalAmount)) totalAmount = 0;
        } catch (e) {
            console.error('Error parsing totalAmount', e);
            totalAmount = 0;
        }
        
        html += `
            <tr data-status="${order.status}" data-id="${order.id}">
                <td>#${order.id}</td>
                <td>${order.userName || 'User #' + order.userId}</td>
                <td>${formatDate(order.orderDate)}</td>
                <td>${getOrderItemsCount(order)}</td>
                <td>$${totalAmount.toFixed(2)}</td>
                <td><span class="order-status ${order.status.toLowerCase()}">${order.status}</span></td>
                <td>
                    <button class="btn-primary view-order-btn" data-id="${order.id}">View</button>
                    <button class="btn-secondary update-status-btn" data-id="${order.id}" data-status="${order.status}">Update Status</button>
                </td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    
    return html;
}

// Get order items count
function getOrderItemsCount(order) {
    const items = order.items || order.orderItems || [];
    const totalItems = items.reduce((total, item) => total + item.quantity, 0);
    return `${totalItems} item${totalItems !== 1 ? 's' : ''}`;
}

// Filter orders by status
function filterOrdersByStatus(status) {
    const rows = orderManagementTab.querySelectorAll('.orders-list table tbody tr');
    
    rows.forEach(row => {
        if (status === 'all' || row.getAttribute('data-status') === status) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Search orders
function searchOrders(query) {
    if (!query) {
        // If query is empty, show all orders
        const rows = orderManagementTab.querySelectorAll('.orders-list table tbody tr');
        rows.forEach(row => {
            row.style.display = '';
        });
        return;
    }
    
    query = query.toLowerCase();
    const rows = orderManagementTab.querySelectorAll('.orders-list table tbody tr');
    
    rows.forEach(row => {
        const orderId = row.getAttribute('data-id');
        const customer = row.querySelector('td:nth-child(2)').textContent.toLowerCase();
        
        if (orderId.includes(query) || customer.includes(query)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// View order details
function viewOrderDetails(orderId) {
    const orderDetailsContainer = document.getElementById('order-details');
    if (!orderDetailsContainer) return;
    
    orderDetailsContainer.innerHTML = '<div class="loading">Loading order details...</div>';
    showModal('order-details-modal');
    
    fetch(`api/orders/${orderId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch order details');
            }
            return response.json();
        })
        .then(order => {
            console.log('Order details from server:', order);
            // Store order details in a data attribute for later use
            if (orderDetailsContainer) {
                orderDetailsContainer.setAttribute('data-order-details', JSON.stringify(order));
            }
            displayOrderDetails(order);
        })
        .catch(error => {
            console.error(`Error loading order details for order ${orderId}:`, error);
            
            // Show error message instead of using sample data
            orderDetailsContainer.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Order Details</h3>
                    <p>${error.message}</p>
                    <p>Please try again later or contact support.</p>
                    <button class="btn-primary retry-btn">Retry</button>
                </div>
            `;
            
            // Add retry button functionality
            const retryBtn = orderDetailsContainer.querySelector('.retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', () => {
                    viewOrderDetails(orderId);
                });
            }
        });
}

// Display order details
function displayOrderDetails(order) {
    const orderDetailsContainer = document.getElementById('order-details');
    
    if (!orderDetailsContainer) return;
    
    let itemsHtml = '';
    let totalAmount = 0;
    
    // Ensure order.items exists and is an array
    const items = order.items || order.orderItems || [];
    
    items.forEach(item => {
        const itemTotal = item.price * item.quantity;
        totalAmount += itemTotal;
        
        itemsHtml += `
            <div class="order-item-detail">
                <div class="order-item-info">
                    <p class="item-name">${item.menuItemName}</p>
                    <p class="item-price">$${item.price.toFixed(2)} x ${item.quantity}</p>
                </div>
                <p class="item-total">$${itemTotal.toFixed(2)}</p>
                ${item.specialInstructions ? `<p class="item-instructions">Note: ${item.specialInstructions}</p>` : ''}
            </div>
        `;
    });
    
    // Safely parse the order total amount
    let orderTotal = 0;
    try {
        orderTotal = typeof order.totalAmount === 'number' 
            ? order.totalAmount 
            : parseFloat(order.totalAmount || 0);
        
        if (isNaN(orderTotal)) orderTotal = 0;
    } catch (e) {
        console.error('Error parsing order totalAmount', e);
    }
    
    orderDetailsContainer.innerHTML = `
        <div class="order-detail-header">
            <h3>Order #${order.id}</h3>
            <span class="order-status ${order.status.toLowerCase()}">${order.status}</span>
        </div>
        <div class="order-detail-info">
            <p><strong>Customer:</strong> ${order.userName || 'User #' + order.userId}</p>
            <p><strong>Date:</strong> ${formatDate(order.orderDate)}</p>
            <p><strong>Delivery Address:</strong> ${order.deliveryAddress}</p>
            <p><strong>Payment Method:</strong> ${order.paymentMethod}</p>
            ${order.specialInstructions ? `<p><strong>Special Instructions:</strong> ${order.specialInstructions}</p>` : ''}
        </div>
        <div class="order-detail-items">
            <h4>Items</h4>
            ${itemsHtml}
        </div>
        <div class="order-detail-total">
            <p>Total: $${orderTotal.toFixed(2)}</p>
        </div>
        <div class="order-detail-actions">
            <button class="btn-secondary update-status-btn" data-id="${order.id}" data-status="${order.status}">Update Status</button>
        </div>
    `;
    
    // Add event listener to update status button
    const updateStatusBtn = orderDetailsContainer.querySelector('.update-status-btn');
    if (updateStatusBtn) {
        updateStatusBtn.addEventListener('click', () => {
            const orderId = updateStatusBtn.getAttribute('data-id');
            const currentStatus = updateStatusBtn.getAttribute('data-status');
            updateOrderStatus(orderId, currentStatus);
        });
    }
}

// Get order payment method (helper function)
function getOrderPaymentMethod(orderId) {
    // Try to get order details from the order details container
    const orderDetailsContainer = document.getElementById('order-details');
    if (orderDetailsContainer) {
        const orderDetailsAttr = orderDetailsContainer.getAttribute('data-order-details');
        if (orderDetailsAttr) {
            try {
                const orderDetails = JSON.parse(orderDetailsAttr);
                if (orderDetails && orderDetails.paymentMethod) {
                    return orderDetails.paymentMethod;
                }
            } catch (e) {
                console.error('Error parsing order details', e);
            }
        }
    }
    
    // Default payment method if we can't retrieve the existing one
    return "CREDIT_CARD";
}

// Get complete order details for updates
function getOrderDetails(orderId) {
    // Try to get order details from the order details container
    const orderDetailsContainer = document.getElementById('order-details');
    if (orderDetailsContainer) {
        const orderDetailsAttr = orderDetailsContainer.getAttribute('data-order-details');
        if (orderDetailsAttr) {
            try {
                const orderDetails = JSON.parse(orderDetailsAttr);
                // Ensure totalAmount is preserved as a number
                if (orderDetails) {
                    if (orderDetails.totalAmount) {
                        // Make sure totalAmount is a number
                        orderDetails.totalAmount = typeof orderDetails.totalAmount === 'number' 
                            ? orderDetails.totalAmount 
                            : parseFloat(orderDetails.totalAmount);
                            
                        if (isNaN(orderDetails.totalAmount)) {
                            orderDetails.totalAmount = 0;
                        }
                    } else {
                        // If totalAmount doesn't exist, initialize it to prevent it being lost
                        orderDetails.totalAmount = 0;
                    }
                    return orderDetails;
                }
            } catch (e) {
                console.error('Error parsing order details', e);
            }
        }
    }
    
    // Return a minimal order object if we can't get the full details
    return {
        id: orderId,
        paymentMethod: "CREDIT_CARD",
        totalAmount: 0
    };
}

// Update order status
function updateOrderStatus(orderId, currentStatus) {
    // Strip # from orderId if present
    if (typeof orderId === 'string' && orderId.startsWith('#')) {
        orderId = orderId.substring(1);
    }
    
    console.log(`Updating order ${orderId} status from ${currentStatus}`);
    
    // Create a simple modal with status options
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.id = 'status-update-modal';
    modal.style.display = 'block';
    
    const statusOptions = ['PENDING', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED'];
    let optionsHtml = '';
    
    statusOptions.forEach(status => {
        optionsHtml += `<option value="${status}" ${status === currentStatus ? 'selected' : ''}>${status}</option>`;
    });
    
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <h3>Update Order Status</h3>
            <div class="form-group">
                <label for="order-status">New status for Order #${orderId}:</label>
                <select id="order-status" class="form-control">
                    ${optionsHtml}
                </select>
            </div>
            <div class="form-actions">
                <button id="cancel-status-update" class="btn-secondary">Cancel</button>
                <button id="confirm-status-update" class="btn-primary">Update</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Add event listeners for modal controls
    const closeButton = modal.querySelector('.close-modal');
    const cancelButton = modal.querySelector('#cancel-status-update');
    const confirmButton = modal.querySelector('#confirm-status-update');
    
    function closeModal() {
        document.body.removeChild(modal);
    }
    
    closeButton.addEventListener('click', closeModal);
    cancelButton.addEventListener('click', closeModal);
    
    // When clicking outside the modal
    window.addEventListener('click', function modalOutsideClick(e) {
        if (e.target === modal) {
            closeModal();
            window.removeEventListener('click', modalOutsideClick);
        }
    });
    
    // Handle confirmation
    confirmButton.addEventListener('click', function() {
        const statusSelect = document.getElementById('order-status');
        const newStatus = statusSelect.value;
        
        if (newStatus && newStatus !== currentStatus) {
            // Change button state to show processing
            confirmButton.disabled = true;
            confirmButton.textContent = 'Updating...';
            
            // Show loading indicator
            const loadingModal = document.createElement('div');
            loadingModal.className = 'loading-overlay';
            loadingModal.innerHTML = '<div class="loading-spinner"></div>';
            document.body.appendChild(loadingModal);
            
            // Our improved approach - always get the order first to ensure we have all the data
            fetch(`api/orders/${orderId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to fetch order: ${response.status} ${response.statusText}`);
                    }
                    return response.json();
                })
                .then(orderData => {
                    console.log("Retrieved order data for update:", orderData);
                    
                    // Store the exact totalAmount as received, without parsing
                    const originalTotal = orderData.totalAmount;
                    
                    // Try the status-specific endpoint first
                    return fetch(`api/orders/${orderId}/status`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            status: newStatus,
                            // Include the payment method as it's required
                            paymentMethod: orderData.paymentMethod || 'CREDIT_CARD',
                            // Include the total amount to preserve it - send as string to maintain exact precision
                            totalAmount: originalTotal
                        })
                    })
                    .then(response => {
                        if (!response.ok) {
                            // If the status endpoint fails, use the fallback method
                            console.log("Status endpoint failed, using full update fallback");
                            
                            // Simple update with just the fields we need to change
                            const updateData = {
                                id: orderId,
                                status: newStatus,
                                totalAmount: originalTotal,
                                paymentMethod: orderData.paymentMethod || 'CREDIT_CARD'
                            };
                            
                            console.log("Using fallback update with data:", updateData);
                            
                            return fetch(`api/orders/${orderId}`, {
                                method: 'PUT',
                                headers: {
                                    'Content-Type': 'application/json'
                                },
                                body: JSON.stringify(updateData)
                            });
                        }
                        return response;
                    })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`Failed to update order: ${response.status} ${response.statusText}`);
                        }
                        return response.json();
                    })
                    .then(result => {
                        console.log("Update result:", result);
                        
                        // After successful update, verify the updated order still has the correct total
                        return fetch(`api/orders/${orderId}`)
                            .then(response => response.json())
                            .then(verifiedOrder => {
                                console.log("Verification check - updated order:", verifiedOrder);
                                
                                // Only apply a fix if the total amount was actually lost (set to zero or null)
                                // NOT if it was manually changed to a different non-zero value
                                if ((verifiedOrder.totalAmount === 0 || verifiedOrder.totalAmount === "0" || verifiedOrder.totalAmount === null) && 
                                    originalTotal !== 0 && originalTotal !== "0" && originalTotal !== null) {
                                    console.log("Total amount was lost, making one final fix...");
                                    
                                    // Create a minimal update to restore just the total amount
                                    const fixData = {
                                        id: orderId,
                                        status: newStatus,
                                        totalAmount: originalTotal,
                                        paymentMethod: orderData.paymentMethod || 'CREDIT_CARD'
                                    };
                                    
                                    return fetch(`api/orders/${orderId}`, {
                                        method: 'PUT',
                                        headers: {
                                            'Content-Type': 'application/json'
                                        },
                                        body: JSON.stringify(fixData)
                                    })
                                    .then(response => response.json())
                                    .then(finalResult => {
                                        console.log("Final fix result:", finalResult);
                                        return finalResult;
                                    });
                                }
                                
                                return result;
                            });
                    })
                    .then(() => {
                        document.body.removeChild(loadingModal);
                        closeModal();
                        alert(`Order #${orderId} status updated to: ${newStatus}`);
                        
                        // Refresh the order management view
                        loadOrderManagementContent();
                        
                        // Also refresh order details if modal is open
                        const orderDetailsModal = document.getElementById('order-details-modal');
                        if (orderDetailsModal && orderDetailsModal.style.display === 'block') {
                            viewOrderDetails(orderId);
                        }
                    });
                })
                .catch(error => {
                    console.error("Error updating order status:", error);
                    document.body.removeChild(loadingModal);
                    alert(`Failed to update order status: ${error.message}`);
                    confirmButton.disabled = false;
                    confirmButton.textContent = 'Update';
                });
        } else {
            closeModal();
        }
    });
}

// Show add user form
function showAddUserForm() {
    showModal('add-user-modal');
}

// Add user
function addUser(formData) {
    // Show loading state
    const submitButton = document.querySelector('#add-user-form button[type="submit"]');
    if (submitButton) {
        submitButton.innerHTML = 'Adding User...';
        submitButton.disabled = true;
    }
    
    // Check if passwords match if there's a confirm password field
    const password = formData.get('password');
    const confirmPassword = formData.get('confirmPassword');
    
    if (confirmPassword && password !== confirmPassword) {
        alert('Passwords do not match');
        if (submitButton) {
            submitButton.innerHTML = 'Add User';
            submitButton.disabled = false;
        }
        return;
    }
    
    // Create the user object with the same structure as the auth.js register function
    const userData = {
        username: formData.get('username'),
        email: formData.get('email'),
        password: password,
        role: formData.get('role') || 'CUSTOMER',
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        phone: formData.get('phone'),
        address: formData.get('address')
    };
    
    fetch('api/admin/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(userData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(errorData => {
                throw new Error(errorData.message || 'Failed to add user');
            }).catch(e => {
                // If we can't parse the error as JSON, use the status text
                if (e instanceof SyntaxError) {
                    throw new Error(`Failed to add user: ${response.statusText}`);
                }
                throw e; // Re-throw if it's our custom error
            });
        }
        return response.json();
    })
    .then(data => {
        console.log('User added successfully:', data);
        alert('User added successfully');
        closeAllModals();
        loadUserManagementContent();
    })
    .catch(error => {
        console.error('Error adding user:', error);
        alert(`Error adding user: ${error.message}`);
    })
    .finally(() => {
        // Reset button state
        if (submitButton) {
            submitButton.innerHTML = 'Add User';
            submitButton.disabled = false;
        }
    });
}

// Search users
function searchUsers(query) {
    if (!query) {
        // If query is empty, show all users
        const rows = userManagementTab.querySelectorAll('.users-list table tbody tr');
        rows.forEach(row => {
            row.style.display = '';
        });
        return;
    }
    
    query = query.toLowerCase();
    const rows = userManagementTab.querySelectorAll('.users-list table tbody tr');
    
    rows.forEach(row => {
        const username = row.querySelector('td:nth-child(2)').textContent.toLowerCase();
        const email = row.querySelector('td:nth-child(3)').textContent.toLowerCase();
        
        if (username.includes(query) || email.includes(query)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Filter users by role
function filterUsersByRole(role) {
    const rows = userManagementTab.querySelectorAll('.users-list table tbody tr');
    
    rows.forEach(row => {
        const userRole = row.querySelector('td:nth-child(4) span').textContent;
        
        if (role === 'all' || userRole === role) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Show modal
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    }
}

// Close all modals
function closeAllModals() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
}

// Show add menu item form
function showAddMenuItemForm() {
    showModal('menu-item-modal');
    
    // Clear form fields
    const form = document.getElementById('menu-item-form');
    if (form) {
        form.reset();
        document.getElementById('menu-item-id').value = ''; // Clear id for new item
        document.getElementById('menu-item-form-title').textContent = 'Add New Menu Item';
        document.querySelector('#menu-item-form button[type="submit"]').textContent = 'Add Item';
        
        // Populate categories
        populateMenuItemCategories();
    }
}

// Populate menu item categories
function populateMenuItemCategories() {
    const categorySelect = document.getElementById('menu-item-category');
    if (!categorySelect) return;
    
    // Clear existing options except the first one
    while (categorySelect.options.length > 1) {
        categorySelect.remove(1);
    }
    
    // Add standard categories
    const categories = [
        'Pizza', 'Pasta', 'Salad', 'Main Course', 'Appetizer', 'Dessert', 'Beverage'
    ];
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category;
        option.textContent = category;
        categorySelect.appendChild(option);
    });
}

// Edit menu item
function editMenuItem(itemId) {
    // Strip any non-numeric characters from itemId
    itemId = itemId.toString().replace(/\D/g, '');
    
    
    console.log(`Fetching menu item ${itemId} for editing`);
    
    // Use jQuery AJAX for consistency
    $.ajax({
        url: `api/menu/${itemId}`,
        type: 'GET',
        dataType: 'json',
        success: function(item) {
            console.log('Menu item details from server:', item);
            showEditMenuItemForm(item);
        },
        error: function(xhr, status, error) {
            console.error(`Error loading menu item details for item ${itemId}:`, error);
            console.error('Response:', xhr.responseText);
            alert(`Error: Failed to load menu item details. ${xhr.status}: ${xhr.statusText}`);
        }
    });
}

// Show edit menu item form
function showEditMenuItemForm(item) {
    showModal('menu-item-modal');
    
    // Fill form fields with item data
    const form = document.getElementById('menu-item-form');
    if (form) {
        document.getElementById('menu-item-id').value = item.id;
        document.getElementById('menu-item-name').value = item.name || '';
        document.getElementById('menu-item-description').value = item.description || '';
        document.getElementById('menu-item-price').value = item.price || '';
        document.getElementById('menu-item-image-url').value = item.imageUrl || '';
        
        // Populate categories
        populateMenuItemCategories();
        
        // Select the correct category
        const categorySelect = document.getElementById('menu-item-category');
        if (categorySelect) {
            // First check if the category exists in options
            let categoryExists = false;
            for (let i = 0; i < categorySelect.options.length; i++) {
                if (categorySelect.options[i].value === item.category) {
                    categorySelect.value = item.category;
                    categoryExists = true;
                    break;
                }
            }
            
            // If category doesn't exist, add it
            if (!categoryExists && item.category) {
                const option = document.createElement('option');
                option.value = item.category;
                option.textContent = item.category;
                categorySelect.appendChild(option);
                categorySelect.value = item.category;
            }
        }
        
        // Set available and featured checkboxes
        document.getElementById('menu-item-available').checked = item.available || false;
        document.getElementById('menu-item-featured').checked = item.featured || false;
        
        // Update form title and button text
        document.getElementById('menu-item-form-title').textContent = 'Edit Menu Item';
        document.querySelector('#menu-item-form button[type="submit"]').textContent = 'Save Changes';
    }
}

// Save menu item
function saveMenuItem(formData) {
    const itemId = formData.get('id');
    const isNewItem = !itemId;
    
    // Create item data object
    const itemData = {
        name: formData.get('name'),
        description: formData.get('description'),
        price: parseFloat(formData.get('price')),
        category: formData.get('category'),
        imageUrl: formData.get('imageUrl'),
        available: formData.get('available') === 'on',
        featured: formData.get('featured') === 'on'
    };
    
    // Validate price
    if (isNaN(itemData.price) || itemData.price <= 0) {
        alert('Please enter a valid price');
        return;
    }
    
    // URL and method for API call
    const url = isNewItem ? 'api/menu' : `api/menu/${itemId}`;
    const method = isNewItem ? 'POST' : 'PUT';
    
    // Use jQuery AJAX for consistency with other API calls
    $.ajax({
        url: url,
        type: method,
        contentType: 'application/json',
        data: JSON.stringify(itemData),
        success: function(response) {
            console.log('Menu item saved successfully', response);
            alert(`Menu item ${isNewItem ? 'added' : 'updated'} successfully`);
            closeAllModals();
            loadMenuManagementContent(); // Refresh menu list
        },
        error: function(xhr, status, error) {
            console.error(`Error ${isNewItem ? 'adding' : 'updating'} menu item:`, error);
            console.error('Response:', xhr.responseText);
            alert(`Error: Failed to ${isNewItem ? 'add' : 'update'} menu item. ${xhr.status}: ${xhr.statusText}`);
        }
    });
}

