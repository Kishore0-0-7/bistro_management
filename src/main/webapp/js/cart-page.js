// Cart Page JavaScript

// DOM Elements
const cartContainer = document.getElementById('cart-container');
// Check if cartCount is already defined in another script to avoid duplicate declaration
let cartCount;
if (typeof window.cartCount === 'undefined') {
    cartCount = document.getElementById('cart-count');
    window.cartCount = cartCount; // Store in window object to share across scripts
} else {
    cartCount = window.cartCount;
}

// Initialize cart page
document.addEventListener('DOMContentLoaded', () => {
    console.log('Cart page initializing...');
    console.log('Cart container element found:', cartContainer !== null);
    
    // Set up event listeners
    setupCartEventListeners();
    
    // Load cart items
    loadCartItems();
});

// Set up cart event listeners
function setupCartEventListeners() {
    console.log('Setting up cart event listeners');
    
    // Handle direct click events (not dynamically generated)
    document.addEventListener('click', function(e) {
        console.log('Click detected:', e.target);
        
        // Close modal buttons
        if (e.target && e.target.classList.contains('close-modal')) {
            console.log('Close modal button clicked');
            closeAllModals();
        }
        
        // Login and register buttons
        if (e.target && e.target.id === 'login-btn') {
            console.log('Login button clicked');
            showModal('login-modal');
        }
        
        if (e.target && e.target.id === 'register-btn') {
            console.log('Register button clicked');
            showModal('register-modal');
        }
        
        // Show login/register form links
        if (e.target && e.target.id === 'show-login') {
            console.log('Show login link clicked');
            e.preventDefault();
            closeAllModals();
            showModal('login-modal');
        }
        
        if (e.target && e.target.id === 'show-register') {
            console.log('Show register link clicked');
            e.preventDefault();
            closeAllModals();
            showModal('register-modal');
        }
    });
    
    // Close modal when clicking outside
    window.addEventListener('click', (e) => {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            if (e.target === modal) {
                console.log('Clicked outside modal, closing all modals');
                closeAllModals();
            }
        });
    });
    
    // Checkout form submit handler
    document.addEventListener('submit', function(e) {
        if (e.target && e.target.id === 'checkout-form') {
            e.preventDefault();
            console.log('Checkout form submitted');
            placeOrder();
        }
        
        // Login form
        if (e.target && e.target.id === 'login-form') {
            e.preventDefault();
            console.log('Login form submitted');
            handleLogin();
        }
        
        // Register form
        if (e.target && e.target.id === 'register-form') {
            e.preventDefault();
            console.log('Register form submitted');
            handleRegister();
        }
    });
}

// Add a simple fallback display function
function fallbackDisplayCart(items, total) {
    console.log('Using fallback cart display');
    
    if (!cartContainer) {
        console.error('Cart container not found in fallback display');
        return;
    }
    
    // Create a very simple cart display
    let html = '<div class="cart-fallback">';
    html += '<h3>Your Cart Items</h3>';
    
    if (!items || items.length === 0) {
        html += '<p>Your cart is empty</p>';
    } else {
        html += '<ul>';
        items.forEach(item => {
            try {
                const menuItem = item.menuItem;
                const name = menuItem ? menuItem.name : 'Unknown item';
                const price = menuItem ? menuItem.price : 0;
                const quantity = item.quantity || 1;
                
                html += `<li>${name} - $${parseFloat(price).toFixed(2)} x ${quantity}</li>`;
            } catch (e) {
                html += '<li>Error displaying item</li>';
                console.error('Error in fallback display:', e);
            }
        });
        html += '</ul>';
        html += `<p><strong>Total: $${parseFloat(total || 0).toFixed(2)}</strong></p>`;
    }
    
    html += '<div class="cart-actions">';
    html += '<a href="menu.html" class="btn-primary">Continue Shopping</a>';
    html += '</div></div>';
    
    cartContainer.innerHTML = html;
}

// Load cart items - updated with fallback
function loadCartItems() {
    console.log('Loading cart items...');
    
    if (!cartContainer) {
        console.error('Cart container element not found!');
        return;
    }
    
    // Show loading message
    cartContainer.innerHTML = '<div class="loading">Loading cart items...</div>';
    
    // Fetch cart from server
    fetch('api/cart-service')
        .then(response => {
            console.log('Cart fetch response status:', response.status);
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Server response for cart (full data):', JSON.stringify(data));
            console.log('Cart items count:', data.items ? data.items.length : 0);
            console.log('Cart total:', data.total);
            
            try {
                renderCart(data.items, data.total);
            } catch (error) {
                console.error('Error in normal cart rendering, using fallback:', error);
                fallbackDisplayCart(data.items, data.total);
            }
            
            updateCartCount(data.items);
        })
        .catch(error => {
            console.error('Error loading cart from server:', error);
            
            // Display error to user
            cartContainer.innerHTML = `
                <div class="empty-cart-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <h3>Error Loading Cart</h3>
                    <p>There was a problem loading your cart items.</p>
                    <p>Please try again later.</p>
                    <p class="error-details">Error: ${error.message}</p>
                    <div class="continue-shopping">
                        <a href="menu.html" class="btn-primary">Browse Menu</a>
                    </div>
                </div>
            `;
        });
}

// Render cart
function renderCart(items, total) {
    console.log('Rendering cart with items:', items ? items.length : 0, 'and total:', total);
    
    // Defensive check for undefined/null items or empty array
    if (!items || items.length === 0) {
        console.log('No items to render, showing empty cart message');
        // Display empty cart message
        cartContainer.innerHTML = `
            <div class="empty-cart-message">
                <i class="fas fa-shopping-cart"></i>
                <h3>Your Cart is Empty</h3>
                <p>Looks like you haven't added any items to your cart yet.</p>
                <p>Check out our menu to find something delicious!</p>
                <div class="continue-shopping">
                    <a href="menu.html" class="btn-primary">Browse Menu</a>
                </div>
            </div>
        `;
        return;
    }
    
    console.log('Building HTML for', items.length, 'cart items');
    
    // Render cart items
    let html = `
        <div class="cart-header">
            <h2>Your Items</h2>
        </div>
        <div class="cart-list" id="cart-items">
    `;
    
    // DEBUG: Force stringify and re-parse to ensure we have a proper object
    const safeItems = JSON.parse(JSON.stringify(items));
    
    safeItems.forEach((item, index) => {
        try {
            console.log(`Processing item ${index}:`, item);
            
            // Handle both server and local storage formats
            const menuItem = item.menuItem;
            if (!menuItem) {
                console.error(`Item ${index} has no menuItem property:`, item);
                return;
            }
            
            const name = menuItem.name || 'Unknown Item';
            const price = menuItem.price || 0;
            const itemId = item.menuItemId || 0;
            const quantity = item.quantity || 1;
            
            console.log(`Rendering item ${index}: ${name}, ID: ${itemId}, Price: ${price}, Quantity: ${quantity}`);
            
            html += `
                <div class="cart-item" data-id="${itemId}">
                    <div class="cart-item-info">
                        <h4>${name}</h4>
                        <p>$${parseFloat(price).toFixed(2)} x ${quantity}</p>
                    </div>
                    <div class="cart-item-actions">
                        <button class="decrease-quantity-btn" data-id="${itemId}">-</button>
                        <span class="item-quantity">${quantity}</span>
                        <button class="increase-quantity-btn" data-id="${itemId}">+</button>
                        <button class="remove-item-btn" data-id="${itemId}"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
            `;
        } catch (e) {
            console.error(`Error rendering cart item ${index}:`, e, item);
        }
    });
    
    // Add cart summary and actions
    html += `
        </div>
        <div class="cart-summary">
            <div class="cart-total">
                <span>Total:</span>
                <span>$${parseFloat(total || 0).toFixed(2)}</span>
            </div>
            <div class="cart-actions">
                <button id="clear-cart-btn" class="btn-secondary">Clear Cart</button>
                <button id="checkout-btn" class="btn-primary">Proceed to Checkout</button>
            </div>
        </div>
    `;
    
    // Set cart HTML
    if (cartContainer) {
        cartContainer.innerHTML = html;
        console.log('Cart HTML has been set to the container');
    } else {
        console.error('Cannot set cart HTML - container not found');
    }
    
    // Add event listeners to cart item buttons
    const decreaseBtns = document.querySelectorAll('.decrease-quantity-btn');
    const increaseBtns = document.querySelectorAll('.increase-quantity-btn');
    const removeBtns = document.querySelectorAll('.remove-item-btn');
    const clearCartBtn = document.getElementById('clear-cart-btn');
    const checkoutBtn = document.getElementById('checkout-btn');
    
    // Add event listeners for cart item buttons
    decreaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const itemId = parseInt(this.getAttribute('data-id'));
            console.log('Decreasing quantity for item ID:', itemId);
            updateItemQuantity(itemId, -1);
        });
    });
    
    increaseBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const itemId = parseInt(this.getAttribute('data-id'));
            console.log('Increasing quantity for item ID:', itemId);
            updateItemQuantity(itemId, 1);
        });
    });
    
    removeBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const itemId = parseInt(this.getAttribute('data-id'));
            console.log('Removing item ID:', itemId);
            removeItemFromCart(itemId);
        });
    });
    
    // Clear cart button
    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', function() {
            console.log('Clear cart button clicked');
            clearCart();
        });
    }
    
    // Checkout button
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function() {
            console.log('Checkout button clicked');
            if (isLoggedIn()) {
                showCheckout();
            } else {
                alert('Please login to checkout');
                closeAllModals();
                showModal('login-modal');
            }
        });
    }
    
    console.log('Cart rendered with', items.length, 'items');
}

// Add visual feedback for cart operations
function showItemOperation(itemId, operation) {
    const cartItem = document.querySelector(`.cart-item[data-id="${itemId}"]`);
    if (cartItem) {
        // Add operation class
        cartItem.classList.add(operation);
        
        // Remove class after animation
        setTimeout(() => {
            cartItem.classList.remove(operation);
        }, 1000);
    }
}

// Update cart item quantity
function updateItemQuantity(itemId, change) {
    console.log(`Updating item ${itemId} quantity by ${change}`);
    
    // Show visual feedback immediately
    showItemOperation(itemId, change > 0 ? 'item-increasing' : 'item-decreasing');
    
    // Send update to server
    fetch('api/cart-service', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            menuItemId: itemId,
            quantity: change
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Cart updated on server:', data);
        renderCart(data.items, data.total);
        updateCartCount(data.items);
    })
    .catch(error => {
        console.error('Error updating cart on server:', error);
        alert('Failed to update cart. Please try again.');
    });
}

// Remove item from cart
function removeItemFromCart(itemId) {
    console.log(`Removing item ${itemId} from cart`);
    
    // Confirm removal
    if (!confirm('Are you sure you want to remove this item from your cart?')) {
        return;
    }
    
    // Show visual feedback immediately
    showItemOperation(itemId, 'item-removing');
    
    // Send delete request to server
    fetch(`api/cart-service/${itemId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }
        return response.json();
    })
    .then(data => {
        console.log('Item removed from cart on server:', data);
        renderCart(data.items, data.total);
        updateCartCount(data.items);
    })
    .catch(error => {
        console.error('Error removing item from cart on server:', error);
        alert('Failed to remove item from cart. Please try again.');
    });
}

// Clear cart
function clearCart() {
    // Confirm before clearing
    if (confirm('Are you sure you want to clear your cart?')) {
        console.log('Clearing cart...');
        
        // Send clear request to server
        fetch('api/cart-service', {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Cart cleared on server:', data);
            renderCart(data.items, data.total);
            updateCartCount(data.items);
        })
        .catch(error => {
            console.error('Error clearing cart on server:', error);
            alert('Failed to clear cart. Please try again.');
        });
    }
}

// Update cart count
function updateCartCount(items) {
    if (items) {
        // Update directly from provided items
        const count = items.reduce((total, item) => total + item.quantity, 0);
        
        // Update all cart count elements
        const cartCountElements = document.querySelectorAll('#cart-count');
        cartCountElements.forEach(element => {
            element.textContent = count;
        });
    } else {
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
}

// Show checkout
function showCheckout() {
    console.log('Showing checkout modal');
    showModal('checkout-modal');
    
    // Fetch cart items from server to display in checkout
    fetch('api/cart-service')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            displayCheckoutItems(data.items, data.total);
        })
        .catch(error => {
            console.error('Error loading cart for checkout:', error);
            
            // Display empty cart in checkout
            displayCheckoutItems([], 0);
        });
}

// Display checkout items
function displayCheckoutItems(items, total) {
    const orderItemsContainer = document.getElementById('order-items');
    if (!orderItemsContainer) return;
    
    if (items.length === 0) {
        orderItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        
        const orderTotal = document.getElementById('order-total');
        if (orderTotal) {
            orderTotal.textContent = '0.00';
        }
        
        return;
    }
    
    let html = '';
    
    items.forEach(item => {
        // Handle both server and local formats
        const menuItem = item.menuItem || item;
        const name = menuItem.name || item.name;
        const price = menuItem.price || item.price;
        const quantity = item.quantity;
        const itemTotal = parseFloat(price) * quantity;
        
        html += `
            <div class="order-item">
                <div class="order-item-info">
                    <h4>${name}</h4>
                    <p>$${parseFloat(price).toFixed(2)} x ${quantity}</p>
                </div>
                <div class="order-item-total">
                    <p>$${itemTotal.toFixed(2)}</p>
                </div>
            </div>
        `;
    });
    
    orderItemsContainer.innerHTML = html;
    
    const orderTotal = document.getElementById('order-total');
    if (orderTotal) {
        orderTotal.textContent = parseFloat(total).toFixed(2);
    }
}

// Place order
function placeOrder() {
    // Get form data
    const deliveryAddress = document.getElementById('delivery-address').value;
    const paymentMethod = document.getElementById('payment-method').value;
    const specialInstructions = document.getElementById('special-instructions').value;
    
    // Validate form data
    if (!deliveryAddress) {
        alert('Please enter a delivery address');
        return;
    }
    
    if (!paymentMethod) {
        alert('Please select a payment method');
        return;
    }
    
    // Create order object
    const order = {
        deliveryAddress: deliveryAddress,
        paymentMethod: paymentMethod,
        specialInstructions: specialInstructions
    };
    
    // Show processing indicator
    const submitButton = document.querySelector('#checkout-form button[type="submit"]');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.innerHTML = 'Processing...';
    }
    
    // Send order to server
    fetch('api/orders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(order)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to place order');
        }
        return response.json();
    })
    .then(data => {
        // Show success message
        alert('Order placed successfully!');
        
        // Close checkout modal
        closeAllModals();
        
        // Clear cart by calling the api/cart DELETE endpoint
        fetch('api/cart-service', {
            method: 'DELETE'
        })
        .then(() => {
            // Update cart 
            loadCartItems();
            updateCartCount([]);
            
            // Redirect to orders page
            window.location.href = 'orders.html';
        });
    })
    .catch(error => {
        console.error('Error placing order:', error);
        alert('Failed to place order. Please try again.');
        
        // Reset submit button
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.innerHTML = 'Place Order';
        }
    });
}

// Handle login
function handleLogin() {
    console.log('Handling login');
    
    // Get form data
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    // Store the username in a cookie that will persist even if sessionStorage is cleared
    document.cookie = `bistro_username=${username}; path=/; max-age=86400`;
    
    // Create login object
    const loginData = {
        username: username,
        password: password
    };
    
    // Send login request to server
    fetch('api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(loginData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Login failed');
        }
        return response.json();
    })
    .then(data => {
        // Store user info in session storage
        sessionStorage.setItem('user', JSON.stringify(data.user));
        
        // Also store username separately for easier access
        sessionStorage.setItem('username', username);
        
        // Close login modal
        closeAllModals();
        
        // Update UI to show logged in state
        updateAuthUI();
        
        // Sync cart with server to associate items with user ID
        syncCartWithServer();
        
        // Show checkout if coming from checkout flow
        setTimeout(() => {
            showCheckout();
        }, 500);
    })
    .catch(error => {
        console.error('Error logging in:', error);
        alert('Login failed. Please check your credentials and try again.');
    });
}

// Sync cart with server
function syncCartWithServer() {
    console.log('Syncing cart with server after login');
    
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
        
        // Explicitly sync cart with user account using PUT /sync endpoint
        if (initialData.items && initialData.items.length > 0) {
            console.log('Existing cart found, syncing with user account');
            // Force a sync to associate with user
            return fetch('api/cart-service/sync', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(initialData.items)
            });
        }
        return null;
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
        // Reload cart items
        loadCartItems();
    })
    .catch(error => {
        console.error('Error syncing cart with server:', error);
    });
}

// Handle register
function handleRegister() {
    console.log('Handling registration');
    
    // Get form data
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;
    
    // Validate passwords match
    if (password !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }
    
    // Create registration object
    const registrationData = {
        username: username,
        email: email,
        password: password
    };
    
    // Send registration request to server
    fetch('api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(registrationData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Registration failed');
        }
        return response.json();
    })
    .then(data => {
        // Store user info in session storage
        sessionStorage.setItem('user', JSON.stringify(data));
        
        // Close registration modal
        closeAllModals();
        
        // Update UI to show logged in state
        updateAuthUI();
        
        // Show checkout if coming from checkout flow
        setTimeout(() => {
            showCheckout();
        }, 500);
    })
    .catch(error => {
        console.error('Error registering:', error);
        alert('Registration failed. Please try again.');
    });
}

// Update auth UI
function updateAuthUI() {
    const user = JSON.parse(sessionStorage.getItem('user'));
    
    if (user) {
        // Hide login/register buttons
        document.getElementById('login-btn').classList.add('hidden');
        document.getElementById('register-btn').classList.add('hidden');
        
        // Show user profile
        const userProfile = document.getElementById('user-profile');
        userProfile.classList.remove('hidden');
        
        // Set username
        document.getElementById('username-display').textContent = user.username;
        
        // Show orders link
        document.getElementById('orders-link').classList.remove('hidden');
        
        // Show admin link if admin user
        if (user.role === 'ADMIN') {
            document.getElementById('admin').classList.remove('hidden');
        }
    } else {
        // Show login/register buttons
        document.getElementById('login-btn').classList.remove('hidden');
        document.getElementById('register-btn').classList.remove('hidden');
        
        // Hide user profile
        document.getElementById('user-profile').classList.add('hidden');
        
        // Hide orders link
        document.getElementById('orders-link').classList.add('hidden');
        
        // Hide admin link
        document.getElementById('admin').classList.add('hidden');
    }
}

// Check if user is logged in
function isLoggedIn() {
    return sessionStorage.getItem('user') !== null;
}

// Show modal
function showModal(modalId) {
    console.log('Showing modal:', modalId);
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    } else {
        console.error('Modal not found:', modalId);
    }
}

// Close all modals
function closeAllModals() {
    console.log('Closing all modals');
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check and update authentication UI
    updateAuthUI();
}); 