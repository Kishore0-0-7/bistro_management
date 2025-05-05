// Cart script

// DOM Elements
const cartButtonElement = document.getElementById('cart-btn');
// Check if cartCount exists globally before defining it
let cartCount;
if (typeof window.cartCount === 'undefined') {
    cartCount = document.getElementById('cart-count');
    window.cartCount = cartCount; // Store in window object to share across scripts
} else {
    cartCount = window.cartCount;
}
const cartItemsContainer = document.getElementById('cart-items');
const cartTotal = document.getElementById('cart-total');
const clearCartBtn = document.getElementById('clear-cart-btn');
const checkoutBtn = document.getElementById('checkout-btn');
const orderItemsContainer = document.getElementById('order-items');
const orderTotal = document.getElementById('order-total');
const checkoutForm = document.getElementById('checkout-form');

// Initialize cart
document.addEventListener('DOMContentLoaded', () => {
    console.log('Cart script initializing...');
    
    // Set up event listeners
    setupCartEventListeners();
    
    // Load cart items
    loadCartItems();
    
    // Update cart count
    updateCartCount();
});

// Set up cart event listeners
function setupCartEventListeners() {
    // Using direct event listeners for buttons in the cart modal header
    if (clearCartBtn) {
        console.log('Adding direct event listener to clear cart button');
        clearCartBtn.addEventListener('click', clearCart);
    }
    
    if (checkoutBtn) {
        console.log('Adding direct event listener to checkout button');
        checkoutBtn.addEventListener('click', () => {
            if (isLoggedIn()) {
                showCheckout();
            } else {
                alert('Please login to checkout');
                closeAllModals();
                showModal('login-modal');
            }
        });
    }
    
    // For dynamically created cart items, use event delegation
    console.log('Setting up cart item button delegation on document');
    document.addEventListener('click', function(e) {
        console.log('Click detected:', e.target);
        
        // Cart button
        if (e.target && (e.target.id === 'cart-btn' || e.target.closest('#cart-btn'))) {
            console.log('Cart button clicked via delegation');
            loadCartItems();
            showModal('cart-modal');
        }
        
        // Fallback for Clear cart button
        if (e.target && (e.target.id === 'clear-cart-btn' || e.target.closest('#clear-cart-btn'))) {
            console.log('Clear cart button clicked via delegation');
            clearCart();
        }
        
        // Fallback for Checkout button
        if (e.target && (e.target.id === 'checkout-btn' || e.target.closest('#checkout-btn'))) {
            console.log('Checkout button clicked via delegation');
            if (isLoggedIn()) {
                showCheckout();
            } else {
                alert('Please login to checkout');
                closeAllModals();
                showModal('login-modal');
            }
        }
        
        // Button event listeners for cart item actions
        if (e.target && e.target.classList.contains('decrease-quantity-btn')) {
            console.log('Decrease quantity button clicked');
            const itemId = parseInt(e.target.getAttribute('data-id'));
            console.log('Decreasing quantity for item ID:', itemId);
            updateItemQuantity(itemId, -1);
        }
        
        if (e.target && e.target.classList.contains('increase-quantity-btn')) {
            console.log('Increase quantity button clicked');
            const itemId = parseInt(e.target.getAttribute('data-id'));
            console.log('Increasing quantity for item ID:', itemId);
            updateItemQuantity(itemId, 1);
        }
        
        if (e.target && (e.target.classList.contains('remove-item-btn') || 
                        (e.target.tagName === 'I' && e.target.parentElement && 
                         e.target.parentElement.classList.contains('remove-item-btn')))) {
            console.log('Remove item button clicked');
            const button = e.target.classList.contains('remove-item-btn') ? 
                          e.target : e.target.parentElement;
            const itemId = parseInt(button.getAttribute('data-id'));
            console.log('Removing item ID:', itemId);
            removeItemFromCart(itemId);
        }
        
        // Close modal buttons
        if (e.target && e.target.classList.contains('close-modal')) {
            console.log('Close modal button clicked');
            closeAllModals();
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
    
    // Checkout form
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', (e) => {
            e.preventDefault();
            console.log('Checkout form submitted');
            placeOrder();
        });
    } else {
        // Add checkout form listener if added dynamically
        document.addEventListener('submit', function(e) {
            if (e.target && e.target.id === 'checkout-form') {
                e.preventDefault();
                console.log('Checkout form submitted via delegation');
                placeOrder();
            }
        });
    }
}

// Load cart items
function loadCartItems() {
    console.log('Loading cart items...');
    
    // Get cart items container
    if (!cartItemsContainer) {
        console.error('Cart items container not found');
        return;
    }
    
    // Show loading message
    cartItemsContainer.innerHTML = '<p class="loading">Loading cart items...</p>';
    
    // Fetch cart from server
    fetch('api/cart-service')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Server response for cart:', data);
            updateCartView(data.items, data.total);
        })
        .catch(error => {
            console.error('Error loading cart from server:', error);
            
            // Empty cart if API fails
            updateCartView([], 0);
            
            // Display error to user
            alert('Error loading cart from server. Please try again later.');
        });
}

// Update cart view with items and total
function updateCartView(items, total) {
    if (!cartItemsContainer) return;
    
    if (!items || items.length === 0) {
        cartItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        
        if (cartTotal) {
            cartTotal.textContent = '0.00';
        }
        
        if (checkoutBtn) {
            checkoutBtn.disabled = true;
        }
        
        return;
    }
    
    let html = '';
    
    items.forEach(item => {
        // Handle both server and local storage formats
        const menuItem = item.menuItem || item;
        const name = menuItem.name || item.name;
        const price = menuItem.price || item.price;
        const itemId = item.menuItemId || item.id;
        
        html += `
            <div class="cart-item" data-id="${itemId}">
                <div class="cart-item-info">
                    <h4>${name}</h4>
                    <p>$${parseFloat(price).toFixed(2)} x ${item.quantity}</p>
                </div>
                <div class="cart-item-actions">
                    <button class="decrease-quantity-btn" data-id="${itemId}">-</button>
                    <span class="item-quantity">${item.quantity}</span>
                    <button class="increase-quantity-btn" data-id="${itemId}">+</button>
                    <button class="remove-item-btn" data-id="${itemId}"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    });
    
    console.log('Setting cart HTML:', html);
    cartItemsContainer.innerHTML = html;
    
    if (cartTotal) {
        cartTotal.textContent = parseFloat(total).toFixed(2);
    }
    
    if (checkoutBtn) {
        checkoutBtn.disabled = false;
    }
    
    console.log('Cart view updated. Items:', items.length);
}

// Update cart item quantity
function updateItemQuantity(itemId, change) {
    console.log(`Updating item ${itemId} quantity by ${change}`);
    
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
        updateCartView(data.items, data.total);
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
        updateCartView(data.items, data.total);
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
            updateCartView(data.items, data.total);
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
    closeAllModals();
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
    if (!orderItemsContainer) return;
    
    if (items.length === 0) {
        orderItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        
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
    
    // Get total amount - ensure it's a proper number
    const totalString = document.getElementById('order-total').textContent;
    const totalAmount = parseFloat(totalString || '0');
    
    console.log('Placing order with total amount:', totalAmount);
    
    // First fetch the current cart items to include in the order
    fetch('api/cart-service')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(cartData => {
            // Convert cart items to order items format
            const orderItems = cartData.items.map(item => {
                const menuItem = item.menuItem || item;
                return {
                    menuItemId: item.menuItemId || menuItem.id,
                    menuItemName: menuItem.name,
                    quantity: item.quantity,
                    price: menuItem.price,
                    specialInstructions: item.specialInstructions || ''
                };
            });
            
            // Create order object with items included
            const order = {
                deliveryAddress: deliveryAddress,
                paymentMethod: paymentMethod,
                specialInstructions: specialInstructions,
                totalAmount: totalAmount,
                orderItems: orderItems  // Include the order items
            };
            
            console.log('Placing order with items:', orderItems.length);
            
            // Show loading state
            const checkoutBtn = document.querySelector('#checkout-form button[type="submit"]');
            if (checkoutBtn) {
                checkoutBtn.disabled = true;
                checkoutBtn.textContent = 'Processing...';
            }
            
            // Send order to server
            return fetch('api/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(order)
            });
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to place order');
            }
            return response.json();
        })
        .then(data => {
            // Extract order details from the response
            const orderData = data.order || {};
            const orderId = orderData.id || 'N/A';
            
            // Show success message with custom notification
            showNotification('Success', `Order #${orderId} placed successfully!`, 'success');
            
            // Close checkout modal
            closeAllModals();
            
            // Clear cart by calling the api/cart DELETE endpoint
            fetch('api/cart-service', {
                method: 'DELETE'
            })
            .then(() => {
                // Update cart count to zero
                updateCartCount([]);
                
                // Redirect to orders page
                setTimeout(() => {
                    window.location.href = 'orders.html';
                }, 1500);
            });
        })
        .catch(error => {
            console.error('Error placing order:', error);
            showNotification('Error', 'Failed to place order. Please try again.', 'error');
            
            // Re-enable the checkout button
            const checkoutBtn = document.querySelector('#checkout-form button[type="submit"]');
            if (checkoutBtn) {
                checkoutBtn.disabled = false;
                checkoutBtn.textContent = 'Place Order';
            }
        });
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

