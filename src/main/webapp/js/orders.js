const ordersListContainer = document.getElementById('orders-list');
const orderDetailsModal = document.getElementById('order-details-modal');
const orderDetailsContainer = document.getElementById('order-details');

// Initialize orders
document.addEventListener('DOMContentLoaded', () => {
    console.log('Orders script initializing...');
    
    // Load user orders if user is logged in
    if (isLoggedIn()) {
        loadUserOrders();
    }
});

// Load user orders
function loadUserOrders() {
    console.log('Loading user orders...');
    
    if (!ordersListContainer) {
        console.error('Orders list container not found');
        return;
    }
    
    ordersListContainer.innerHTML = '<div class="loading">Loading orders...</div>';
    
    // Check if user is logged in
    if (!isLoggedIn()) {
        console.error('User is not logged in');
        ordersListContainer.innerHTML = '<p class="no-orders">Please log in to view your orders</p>';
        return;
    }
    
    // Get the user from session storage
    const user = JSON.parse(sessionStorage.getItem('user'));
    console.log('Loading orders for user:', user.username);
    
    fetch('api/orders')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('You are not authorized to view these orders');
                } else if (response.status === 404) {
                    throw new Error('No orders found');
                } else {
                    throw new Error('Failed to fetch orders');
                }
            }
            return response.json();
        })
        .then(orders => {
            console.log('Orders loaded successfully:', orders);
            displayOrders(orders);
        })
        .catch(error => {
            console.error('Error loading orders:', error);
            
            // Show error message
            ordersListContainer.innerHTML = `<p class="error">Error loading orders: ${error.message}</p>`;
        });
}

// Display orders
function displayOrders(orders) {
    console.log('Displaying orders:', orders);
    
    if (!ordersListContainer) {
        console.error('Orders list container not found');
        return;
    }
    
    if (orders.length === 0) {
        ordersListContainer.innerHTML = '<p class="no-orders">You have no orders yet</p>';
        return;
    }
    
    ordersListContainer.innerHTML = '';
    
    orders.forEach(order => {
        const orderElement = createOrderElement(order);
        ordersListContainer.appendChild(orderElement);
    });
}

// Create order element
function createOrderElement(order) {
    console.log('Creating order element for:', order);
    
    const orderElement = document.createElement('div');
    orderElement.className = 'order-card';
    orderElement.setAttribute('data-id', order.id);
    
    // Format the date
    const orderDate = new Date(order.orderDate);
    const formattedDate = formatDate(orderDate);
    
    // Get items summary
    let itemsSummary = '';
    if (order.orderItems && order.orderItems.length > 0) {
        // Get first item and count
        const firstItem = order.orderItems[0].menuItemName;
        const itemCount = order.orderItems.reduce((total, item) => total + item.quantity, 0);
        
        if (order.orderItems.length === 1) {
            itemsSummary = `${firstItem} × ${order.orderItems[0].quantity}`;
        } else {
            itemsSummary = `${firstItem} and ${itemCount - order.orderItems[0].quantity} more items`;
        }
    } else {
        itemsSummary = 'No items';
    }
    
    // Get status icon
    const statusIcon = getStatusIcon(order.status);
    
    // Get total amount - improved parsing
    let totalAmount = 0;
    
    console.log(`Raw totalAmount for order ${order.id}:`, order.totalAmount, typeof order.totalAmount);
    
    // Handle all possible totalAmount formats
    if (order.totalAmount !== undefined && order.totalAmount !== null) {
        if (typeof order.totalAmount === 'number') {
            totalAmount = order.totalAmount;
        } else if (typeof order.totalAmount === 'string') {
            totalAmount = parseFloat(order.totalAmount);
        } else if (typeof order.totalAmount === 'object' && order.totalAmount.value !== undefined) {
            // Handle case where totalAmount might be a JSON object with a value property (BigDecimal serialization)
            totalAmount = parseFloat(order.totalAmount.value);
        }
    }
    
    // Handle parsing errors or invalid values
    if (isNaN(totalAmount)) {
        console.error(`Invalid totalAmount for order ${order.id}:`, order.totalAmount);
        totalAmount = 0;
    }
    
    console.log(`Parsed totalAmount for order ${order.id}:`, totalAmount);
    
    orderElement.innerHTML = `
        <div class="order-header">
            <h3>Order #${order.id}</h3>
            <span class="order-date">${formattedDate}</span>
        </div>
        <div class="order-status-banner ${order.status.toLowerCase()}">
            <i class="${statusIcon}"></i>
            <span>${formatStatus(order.status)}</span>
        </div>
        <div class="order-details">
            <div class="order-items-summary">
                <p class="summary-label">Items:</p>
                <p class="summary-value">${itemsSummary}</p>
            </div>
            <div class="order-price-delivery">
                <div class="price-box">
                    <p class="detail-label">Total</p>
                    <p class="detail-value">₹${totalAmount.toFixed(2)}</p>
                </div>
                <div class="delivery-box">
                    <p class="detail-label">Delivery</p>
                    <p class="detail-value">${order.deliveryAddress ? order.deliveryAddress.split(',')[0] : 'N/A'}</p>
                </div>
            </div>
        </div>
        <div class="order-actions">
            <button class="btn-primary view-order-btn" data-id="${order.id}">
                <i class="fas fa-eye"></i> View Details
            </button>
            ${order.status === 'PENDING' ? `
                <button class="btn-danger cancel-order-btn" data-id="${order.id}">
                    <i class="fas fa-times"></i> Cancel Order
                </button>
            ` : ''}
            <button class="btn-danger delete-order-btn" data-id="${order.id}">
                <i class="fas fa-trash"></i> Delete
            </button>
        </div>
    `;
    
    // Add event listener to view details button
    orderElement.querySelector('.view-order-btn').addEventListener('click', () => {
        viewOrderDetails(order.id);
    });
    
    // Add event listener to cancel button if present
    const cancelBtn = orderElement.querySelector('.cancel-order-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            if (confirm('Are you sure you want to cancel this order?')) {
                cancelOrder(order.id);
            }
        });
    }
    
    // Add event listener to delete button
    const deleteBtn = orderElement.querySelector('.delete-order-btn');
    deleteBtn.addEventListener('click', () => {
        if (confirm('Are you sure you want to permanently delete this order? This action cannot be undone.')) {
            deleteOrder(order.id);
        }
    });
    
    return orderElement;
}

// View order details
function viewOrderDetails(orderId) {
    console.log('Viewing order details for order:', orderId);
    
    if (!orderDetailsContainer) {
        console.error('Order details container not found');
        return;
    }
    
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
            // No longer need to modify the order's totalAmount here as it should be correct from the server
            displayOrderDetails(order);
        })
        .catch(error => {
            console.error(`Error loading order details for order ${orderId}:`, error);
            orderDetailsContainer.innerHTML = '<p class="error">Failed to load order details: ' + error.message + '</p>';
        });
}

// Display order details
function displayOrderDetails(order) {
    console.log('Displaying order details:', order);
    
    if (!orderDetailsContainer) {
        console.error('Order details container not found');
        return;
    }
    
    let itemsHtml = '';
    let itemCount = 0;
    
    // Ensure order.items exists and is an array
    const items = order.orderItems || [];
    
    items.forEach(item => {
        const itemPrice = parseFloat(item.price);
        const itemTotal = itemPrice * item.quantity;
        itemCount += item.quantity;
        
        itemsHtml += `
            <div class="order-item-detail">
                <div class="order-item-info">
                    <p class="item-name">${item.menuItemName}</p>
                    <p class="item-price">₹${itemPrice.toFixed(2)} × ${item.quantity}</p>
                    ${item.specialInstructions ? `<p class="item-instructions"><i class="fas fa-info-circle"></i> ${item.specialInstructions}</p>` : ''}
                </div>
                <p class="item-total">₹${itemTotal.toFixed(2)}</p>
            </div>
        `;
    });
    
    // If no items, display a message
    if (items.length === 0) {
        itemsHtml = '<div class="no-items-message">No item details available</div>';
    }
    
    // Get total amount - improved parsing
    let finalTotal = 0;
    
    console.log(`Raw totalAmount for order detail ${order.id}:`, order.totalAmount, typeof order.totalAmount);
    
    // Handle all possible totalAmount formats
    if (order.totalAmount !== undefined && order.totalAmount !== null) {
        if (typeof order.totalAmount === 'number') {
            finalTotal = order.totalAmount;
        } else if (typeof order.totalAmount === 'string') {
            finalTotal = parseFloat(order.totalAmount);
        } else if (typeof order.totalAmount === 'object' && order.totalAmount.value !== undefined) {
            // Handle case where totalAmount might be a JSON object with a value property (BigDecimal serialization)
            finalTotal = parseFloat(order.totalAmount.value);
        }
    }
    
    // Handle parsing errors or invalid values
    if (isNaN(finalTotal)) {
        console.error(`Invalid totalAmount for order ${order.id}:`, order.totalAmount);
        finalTotal = 0;
    }
    
    console.log(`Parsed totalAmount for order detail ${order.id}:`, finalTotal);
    
    // Get payment status badge
    let paymentStatusBadge = '';
    if (order.paymentStatus) {
        const statusClass = order.paymentStatus.toLowerCase();
        paymentStatusBadge = `<span class="payment-status ${statusClass}">${formatStatus(order.paymentStatus)}</span>`;
    }
    
    // Format dates
    const orderDate = formatDate(order.orderDate);
    const deliveryDate = order.deliveryDate ? formatDate(order.deliveryDate) : 'Not delivered yet';
    
    orderDetailsContainer.innerHTML = `
        <div class="order-detail-header">
            <div class="order-detail-title">
                <h3>Order #${order.id}</h3>
                <div class="order-meta">
                    <span class="order-status ${order.status.toLowerCase()}">
                        <i class="${getStatusIcon(order.status)}"></i> ${formatStatus(order.status)}
                    </span>
                    ${paymentStatusBadge}
                </div>
            </div>
            <div class="order-detail-summary">
                <div class="summary-item">
                    <span class="summary-label"><i class="fas fa-calendar"></i> Ordered</span>
                    <span class="summary-value">${orderDate}</span>
                </div>
                <div class="summary-item">
                    <span class="summary-label"><i class="fas fa-truck"></i> Delivery</span>
                    <span class="summary-value">${deliveryDate}</span>
                </div>
                <div class="summary-item">
                    <span class="summary-label"><i class="fas fa-box"></i> Items</span>
                    <span class="summary-value">${itemCount} item${itemCount !== 1 ? 's' : ''}</span>
                </div>
            </div>
        </div>
        
        <div class="order-detail-section">
            <h4><i class="fas fa-map-marker-alt"></i> Delivery Information</h4>
            <div class="detail-content">
                <p><strong>Address:</strong> ${order.deliveryAddress || 'Not provided'}</p>
                <p><strong>Payment Method:</strong> ${order.paymentMethod || 'Not specified'}</p>
                ${order.specialInstructions ? 
                    `<div class="special-instructions">
                        <p><strong>Special Instructions:</strong></p>
                        <div class="instructions-box">
                            <i class="fas fa-quote-left"></i>
                            <p>${order.specialInstructions}</p>
                        </div>
                    </div>` : ''
                }
            </div>
        </div>
        
        <div class="order-detail-section">
            <h4><i class="fas fa-utensils"></i> Order Items</h4>
            <div class="order-detail-items">
                ${itemsHtml}
            </div>
            <div class="order-detail-total">
                <div class="subtotal">
                    <span>Subtotal:</span>
                    <span>₹${finalTotal.toFixed(2)}</span>
                </div>
                <div class="delivery-fee">
                    <span>Delivery Fee:</span>
                    <span>₹0.00</span>
                </div>
                <div class="grand-total">
                    <span>Total:</span>
                    <span>₹${finalTotal.toFixed(2)}</span>
                </div>
            </div>
        </div>
        
        ${order.status === 'PENDING' ? `
            <div class="order-detail-actions">
                <button class="btn-danger cancel-order-btn" data-id="${order.id}">
                    <i class="fas fa-times"></i> Cancel Order
                </button>
            </div>
        ` : ''}
    `;
    
    // Add event listener to cancel button if present
    const cancelBtn = orderDetailsContainer.querySelector('.cancel-order-btn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            if (confirm('Are you sure you want to cancel this order?')) {
                cancelOrder(order.id);
            }
        });
    }
}

// Cancel order
function cancelOrder(orderId) {
    console.log('Cancelling order:', orderId);
    
    // Show loading state
    const loadingModal = document.createElement('div');
    loadingModal.className = 'loading-overlay';
    loadingModal.innerHTML = '<div class="loading-spinner"></div><p>Cancelling order...</p>';
    document.body.appendChild(loadingModal);
    
    // Use DELETE endpoint to cancel the order
    fetch(`api/orders/${orderId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        // Remove loading overlay
        document.body.removeChild(loadingModal);
        
        if (!response.ok) {
            console.error(`Error cancelling order. Status: ${response.status}`);
            // Try to get detailed error message if possible
            return response.json().then(errorData => {
                throw new Error(errorData.message || `Failed to cancel order. Status: ${response.status}`);
            }).catch(e => {
                // If can't parse JSON, use generic error
                throw new Error(`Failed to cancel order. Status: ${response.status}`);
            });
        }
        return response.json();
    })
    .then(data => {
        // Show success message with custom modal
        showNotification('Success', 'Order cancelled successfully', 'success');
        closeAllModals();
        
        // Refresh the orders list
        loadUserOrders();
    })
    .catch(error => {
        // Remove loading overlay if still present
        if (document.body.contains(loadingModal)) {
            document.body.removeChild(loadingModal);
        }
        
        console.error(`Error cancelling order ${orderId}:`, error);
        
        // Show error message with custom modal
        showNotification('Error', 'Failed to cancel order: ' + error.message, 'error');
    });
}

// Delete order
function deleteOrder(orderId) {
    console.log('Deleting order:', orderId);
    
    // Show loading state
    const loadingModal = document.createElement('div');
    loadingModal.className = 'loading-overlay';
    loadingModal.innerHTML = '<div class="loading-spinner"></div><p>Deleting order...</p>';
    document.body.appendChild(loadingModal);
    
    // Use custom endpoint to permanently delete the order
    fetch(`api/orders/${orderId}/permanent-delete`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        // Remove loading overlay
        document.body.removeChild(loadingModal);
        
        if (!response.ok) {
            console.error(`Error deleting order. Status: ${response.status}`);
            // Try to get detailed error message if possible
            return response.json().then(errorData => {
                throw new Error(errorData.message || `Failed to delete order. Status: ${response.status}`);
            }).catch(e => {
                // If can't parse JSON, use generic error
                throw new Error(`Failed to delete order. Status: ${response.status}`);
            });
        }
        return response.json();
    })
    .then(data => {
        // Show success message with custom modal
        showNotification('Success', 'Order permanently deleted from the database', 'success');
        closeAllModals();
        
        // Refresh the orders list
        loadUserOrders();
    })
    .catch(error => {
        // Remove loading overlay if still present
        if (document.body.contains(loadingModal)) {
            document.body.removeChild(loadingModal);
        }
        
        console.error(`Error deleting order ${orderId}:`, error);
        
        // Show error message with custom modal
        showNotification('Error', 'Failed to delete order: ' + error.message, 'error');
    });
}

// Show custom notification
function showNotification(title, message, type = 'info') {
    const notificationContainer = document.createElement('div');
    notificationContainer.className = `notification ${type}`;
    
    notificationContainer.innerHTML = `
        <div class="notification-header">
            <h3>${title}</h3>
            <span class="notification-close">&times;</span>
        </div>
        <div class="notification-body">
            <p>${message}</p>
        </div>
    `;
    
    document.body.appendChild(notificationContainer);
    
    // Add close button functionality
    const closeBtn = notificationContainer.querySelector('.notification-close');
    closeBtn.addEventListener('click', () => {
        document.body.removeChild(notificationContainer);
    });
    
    // Auto close after 5 seconds
    setTimeout(() => {
        if (document.body.contains(notificationContainer)) {
            document.body.removeChild(notificationContainer);
        }
    }, 5000);
}

// Update order status
function updateOrderStatus(orderId, currentStatus) {
    console.log('Updating order status for order:', orderId);
    
    const statusOptions = ['PENDING', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED'];
    
    // Create a modal for status update
    const statusModal = document.createElement('div');
    statusModal.className = 'modal status-update-modal';
    statusModal.style.display = 'block';
    
    // Create modal content
    let statusOptionsHtml = '';
    statusOptions.forEach(status => {
        const isSelected = status === currentStatus;
        statusOptionsHtml += `
            <div class="status-option ${isSelected ? 'selected' : ''}" data-status="${status}">
                <div class="status-icon ${status.toLowerCase()}">
                    <i class="${getStatusIcon(status)}"></i>
                </div>
                <div class="status-label">
                    <span>${formatStatus(status)}</span>
                    ${isSelected ? '<span class="current-label">(Current)</span>' : ''}
                </div>
            </div>
        `;
    });
    
    statusModal.innerHTML = `
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <h2>Update Order Status</h2>
            <p>Order #${orderId} - Current Status: <span class="order-status ${currentStatus.toLowerCase()}">${formatStatus(currentStatus)}</span></p>
            <div class="status-options-container">
                ${statusOptionsHtml}
            </div>
            <div class="modal-footer">
                <button class="btn-secondary cancel-btn">Cancel</button>
                <button class="btn-primary update-btn" disabled>Update</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(statusModal);
    
    // Add event listeners
    const closeBtn = statusModal.querySelector('.close-modal');
    const cancelBtn = statusModal.querySelector('.cancel-btn');
    const updateBtn = statusModal.querySelector('.update-btn');
    const statusOptionElements = statusModal.querySelectorAll('.status-option');
    
    let selectedStatus = null;
    
    // Status option selection
    statusOptionElements.forEach(option => {
        option.addEventListener('click', () => {
            const status = option.getAttribute('data-status');
            
            // Skip if clicking on the current status
            if (status === currentStatus) {
                return;
            }
            
            // Remove selected class from all options
            statusOptionElements.forEach(opt => opt.classList.remove('selected'));
            
            // Add selected class to clicked option
            option.classList.add('selected');
            
            // Enable update button
            updateBtn.removeAttribute('disabled');
            
            // Set selected status
            selectedStatus = status;
        });
    });
    
    // Close modal on close button click
    closeBtn.addEventListener('click', () => {
        document.body.removeChild(statusModal);
    });
    
    // Close modal on cancel button click
    cancelBtn.addEventListener('click', () => {
        document.body.removeChild(statusModal);
    });
    
    // Update status on update button click
    updateBtn.addEventListener('click', () => {
        if (selectedStatus && selectedStatus !== currentStatus) {
            // Show loading state
            updateBtn.textContent = 'Updating...';
            updateBtn.disabled = true;
            
            fetch(`api/orders/${orderId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: selectedStatus })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to update order status');
                }
                return response.json();
            })
            .then(data => {
                document.body.removeChild(statusModal);
                showNotification('Success', `Order status updated to ${formatStatus(selectedStatus)}`, 'success');
                
                // Refresh the orders list
                if (typeof loadOrderManagementContent === 'function') {
                    loadOrderManagementContent();
                } else {
                    loadUserOrders();
                }
            })
            .catch(error => {
                updateBtn.textContent = 'Update';
                updateBtn.disabled = false;
                showNotification('Error', error.message || 'Failed to update order status. Please try again.', 'error');
            });
        }
    });
}

// Helper function to get icon for each status
function getStatusIcon(status) {
    switch(status) {
        case 'PENDING':
            return 'fas fa-clock';
        case 'PREPARING':
            return 'fas fa-utensils';
        case 'READY':
            return 'fas fa-check-circle';
        case 'DELIVERED':
            return 'fas fa-truck';
        case 'CANCELLED':
            return 'fas fa-times-circle';
        default:
            return 'fas fa-question-circle';
    }
}

// Helper function to format status for display
function formatStatus(status) {
    return status.charAt(0) + status.slice(1).toLowerCase();
}

// Format date
function formatDate(dateString) {
    // Parse the incoming date string
    const date = new Date(dateString);
    
    // Get the client's local date (without adjusting for timezone)
    const currentDate = new Date();
    
    // Check if the given date is in the future relative to local time
    // If so, adjust by subtracting a day (common timezone issue with UTC vs local)
    const adjustedDate = new Date(date);
    if (date > currentDate && 
        date.getDate() !== currentDate.getDate()) {
        adjustedDate.setDate(adjustedDate.getDate() - 1);
    }
    
    // Format the date using local timezone settings
    const options = { 
        year: 'numeric', 
        month: 'numeric', 
        day: 'numeric', 
        hour: '2-digit', 
        minute: '2-digit', 
        second: '2-digit',
        hour12: true
    };
    
    // Return the formatted date using the client's locale settings
    return new Intl.DateTimeFormat('en-IN', options).format(adjustedDate);
}

// Check if user is logged in
function isLoggedIn() {
    return sessionStorage.getItem('user') !== null;
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
