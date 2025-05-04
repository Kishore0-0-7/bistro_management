// Menu script

// DOM Elements
const menuItemsContainer = document.getElementById('menu-items');
const menuFilters = document.querySelector('.menu-filters');
const menuSearchInput = document.getElementById('menu-search-input');
const menuSearchBtn = document.getElementById('menu-search-btn');

// Initialize menu
document.addEventListener('DOMContentLoaded', () => {
    console.log('Menu script initializing...');
    
    // Set up event listeners
    setupMenuEventListeners();
    
    // Load menu items only if we're on the menu page
    if (menuItemsContainer) {
        loadMenuItems();
    }
});

// Set up menu event listeners
function setupMenuEventListeners() {
    // Menu search
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
}

// Load menu items
function loadMenuItems() {
    console.log('Loading menu items...');
    
    if (!menuItemsContainer) {
        console.log('Not on menu page, skipping menu items loading');
        return;
    }
    
    menuItemsContainer.innerHTML = '<div class="loading">Loading menu items...</div>';
    
    // Load menu categories first
    loadMenuCategories()
        .then(() => {
            // Then load menu items
            fetch('api/menu')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch menu items');
                    }
                    return response.json();
                })
                .then(items => {
                    displayMenuItems(items);
                })
                .catch(error => {
                    console.error('Error loading menu items:', error);
                    // For demonstration, load sample data if API fails
                    const sampleItems = getSampleMenuItems();
                    displayMenuItems(sampleItems);
                });
        });
}

// Load menu categories
function loadMenuCategories() {
    return new Promise((resolve, reject) => {
        if (!menuFilters) {
            console.error('Menu filters container not found');
            resolve(); // Resolve anyway to continue loading items
            return;
        }
        
        fetch('api/menu/categories')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch menu categories');
                }
                return response.json();
            })
            .then(categories => {
                displayMenuCategories(categories);
                resolve();
            })
            .catch(error => {
                console.error('Error loading menu categories:', error);
                // For demonstration, load sample categories if API fails
                const sampleCategories = ['Pizza', 'Pasta', 'Salad', 'Dessert', 'Beverage'];
                displayMenuCategories(sampleCategories);
                resolve();
            });
    });
}

// Display menu categories
function displayMenuCategories(categories) {
    if (!menuFilters) return;
    
    // Keep the "All" button
    const allButton = menuFilters.querySelector('.filter-btn[data-category="all"]');
    menuFilters.innerHTML = '';
    
    if (allButton) {
        menuFilters.appendChild(allButton);
    } else {
        const newAllButton = document.createElement('button');
        newAllButton.className = 'filter-btn active';
        newAllButton.setAttribute('data-category', 'all');
        newAllButton.textContent = 'All';
        menuFilters.appendChild(newAllButton);
    }
    
    // Add category buttons
    categories.forEach(category => {
        const button = document.createElement('button');
        button.className = 'filter-btn';
        button.setAttribute('data-category', category);
        button.textContent = category;
        menuFilters.appendChild(button);
    });
    
    // Add event listeners to filter buttons
    const filterButtons = menuFilters.querySelectorAll('.filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all filter buttons
            filterButtons.forEach(btn => {
                btn.classList.remove('active');
            });
            
            // Add active class to clicked button
            button.classList.add('active');
            
            // Filter menu items
            const category = button.getAttribute('data-category');
            filterMenuItems(category);
        });
    });
}

// Display menu items
function displayMenuItems(items) {
    if (!menuItemsContainer) return;
    
    menuItemsContainer.innerHTML = '';
    
    if (items.length === 0) {
        menuItemsContainer.innerHTML = '<p class="no-items">No menu items found</p>';
        return;
    }
    
    // Store items in a data attribute for filtering
    menuItemsContainer.setAttribute('data-items', JSON.stringify(items));
    
    items.forEach(item => {
        const itemElement = createMenuItemElement(item);
        menuItemsContainer.appendChild(itemElement);
    });
}

// Create a menu item element
function createMenuItemElement(item) {
    const menuItem = document.createElement('div');
    menuItem.className = 'menu-item';
    menuItem.setAttribute('data-category', item.category);
    menuItem.innerHTML = `
        <div class="menu-item-image">
            <img src="${item.imageUrl || 'images/menu/default.jpg'}" alt="${item.name}">
        </div>
        <div class="menu-item-content">
            <h3>${item.name}</h3>
            <p>${item.description}</p>
            <div class="menu-item-footer">
                <span class="menu-item-price">$${item.price.toFixed(2)}</span>
                <button class="add-to-cart-btn" data-id="${item.id}">Add to Cart</button>
            </div>
        </div>
    `;
    
    // Add event listener to add to cart button
    menuItem.querySelector('.add-to-cart-btn').addEventListener('click', () => {
        addToCart(item);
    });
    
    return menuItem;
}

// Filter menu items by category
function filterMenuItems(category) {
    const menuItems = menuItemsContainer.querySelectorAll('.menu-item');
    
    if (category === 'all') {
        menuItems.forEach(item => {
            item.style.display = 'block';
        });
    } else {
        menuItems.forEach(item => {
            if (item.getAttribute('data-category') === category) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    }
}

// Search menu items
function searchMenuItems(query) {
    if (!query) {
        // If query is empty, show all items
        filterMenuItems('all');
        return;
    }
    
    const menuItems = menuItemsContainer.querySelectorAll('.menu-item');
    const lowerCaseQuery = query.toLowerCase();
    
    menuItems.forEach(item => {
        const name = item.querySelector('h3').textContent.toLowerCase();
        const description = item.querySelector('p').textContent.toLowerCase();
        
        if (name.includes(lowerCaseQuery) || description.includes(lowerCaseQuery)) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });
}

// Add to cart
function addToCart(item) {
    console.log('Adding to cart:', item);
    
    // Check if user is logged in
    const user = JSON.parse(sessionStorage.getItem('user'));
    if (!user) {
        console.log('User not logged in, showing prompt');
        const shouldLogin = confirm("Please log in to add items to your cart. Would you like to log in now?");
        
        if (shouldLogin) {
            // Store item in sessionStorage to add after login
            sessionStorage.setItem('pendingCartItem', JSON.stringify(item));
            // Show login modal
            showModal('login-modal');
            return;
        } else {
            console.log('User declined to login');
            // Continue as guest
        }
    }
    
    try {
        // Create JSON payload for the cart item
        const cartItem = {
            menuItemId: item.id,
            quantity: 1
        };
        
        console.log('Sending cart item to server:', cartItem);
        
        // Send to server
        fetch('api/cart-service', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cartItem)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Server response:', data);
            
            // Update cart count in UI
            const cartCountElements = document.querySelectorAll('#cart-count');
            const count = data.items.reduce((total, item) => total + item.quantity, 0);
            cartCountElements.forEach(element => {
                element.textContent = count;
            });
            
            // Show success message
            const confirmRedirect = confirm(`${item.name} added to cart. View your cart?`);
            
            // Redirect to cart page if user confirms
            if (confirmRedirect) {
                window.location.href = 'cart.html';
            }
        })
        .catch(error => {
            console.error('Error adding item to cart:', error);
            alert('Error adding item to cart. Please try again.');
        });
    } catch (e) {
        console.error('Exception in addToCart:', e);
        alert('Error adding item to cart. Please try again.');
    }
}

// Helper function to update cart display
function updateCartDisplay(items, totalAmount) {
    console.log('Updating cart display with items:', items);
    
    const cartItemsContainer = document.getElementById('cart-items');
    const cartTotalElement = document.getElementById('cart-total');
    const checkoutBtn = document.getElementById('checkout-btn');
    
    if (!cartItemsContainer) {
        console.error('Cart items container not found');
        return;
    }
    
    if (!items || items.length === 0) {
        cartItemsContainer.innerHTML = '<p class="empty-cart">Your cart is empty</p>';
        
        if (cartTotalElement) {
            cartTotalElement.textContent = '0.00';
        }
        
        if (checkoutBtn) {
            checkoutBtn.disabled = true;
        }
        
        return;
    }
    
    let html = '';
    
    items.forEach(item => {
        // Handle both server response format and local format
        const menuItem = item.menuItem || { name: item.name, price: item.price };
        const itemPrice = menuItem.price || item.price;
        const itemName = menuItem.name || item.name;
        const itemId = item.menuItemId || item.id;
        
        html += `
            <div class="cart-item" data-id="${itemId}">
                <div class="cart-item-info">
                    <h4>${itemName}</h4>
                    <p>$${parseFloat(itemPrice).toFixed(2)} x ${item.quantity}</p>
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
    
    if (cartTotalElement) {
        cartTotalElement.textContent = parseFloat(totalAmount).toFixed(2);
    }
    
    if (checkoutBtn) {
        checkoutBtn.disabled = false;
    }
}

// Setup cart item buttons - we'll use event delegation from cart.js instead
// Keeping this function as a stub for backward compatibility
function setupCartItemButtons() {
    console.log('Cart buttons will be handled by event delegation in cart.js');
}

// Decrease cart item quantity
function decreaseCartItemQuantity(itemId) {
    console.log('Decreasing quantity for item:', itemId);
    
    fetch('api/cart-service', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            menuItemId: itemId,
            quantity: -1
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update cart');
        }
        return response.json();
    })
    .then(data => {
        console.log('Cart updated:', data);
        updateCartDisplay(data.items, data.total);
    })
    .catch(error => {
        console.error('Error updating cart:', error);
        alert('Error updating cart. Please try again.');
    });
}

// Increase cart item quantity
function increaseCartItemQuantity(itemId) {
    console.log('Increasing quantity for item:', itemId);
    
    fetch('api/cart-service', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            menuItemId: itemId,
            quantity: 1
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update cart');
        }
        return response.json();
    })
    .then(data => {
        console.log('Cart updated:', data);
        updateCartDisplay(data.items, data.total);
    })
    .catch(error => {
        console.error('Error updating cart:', error);
        alert('Error updating cart. Please try again.');
    });
}

// Remove cart item
function removeCartItem(itemId) {
    console.log('Removing item from cart:', itemId);
    
    fetch(`api/cart-service/${itemId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to remove item from cart');
        }
        return response.json();
    })
    .then(data => {
        console.log('Cart updated:', data);
        updateCartDisplay(data.items, data.total);
    })
    .catch(error => {
        console.error('Error removing item from cart:', error);
        alert('Error removing item from cart. Please try again.');
    });
}

// Load featured menu items
function loadFeaturedItems() {
    console.log('Loading featured items...');
    
    const featuredItemsContainer = document.getElementById('featured-items');
    if (!featuredItemsContainer) {
        console.error('Featured items container not found');
        return;
    }
    
    featuredItemsContainer.innerHTML = '<div class="loading">Loading featured items...</div>';
    
    fetch('api/menu/featured')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch featured items');
            }
            return response.json();
        })
        .then(items => {
            displayFeaturedItems(items);
        })
        .catch(error => {
            console.error('Error loading featured items:', error);
            // For demonstration, load sample data if API fails
            const sampleItems = getSampleFeaturedItems();
            displayFeaturedItems(sampleItems);
        });
}

// Display featured menu items
function displayFeaturedItems(items) {
    const featuredItemsContainer = document.getElementById('featured-items');
    if (!featuredItemsContainer) {
        console.error('Featured items container not found');
        return;
    }
    
    featuredItemsContainer.innerHTML = '';
    
    if (items.length === 0) {
        featuredItemsContainer.innerHTML = '<p class="no-items">No featured items found</p>';
        return;
    }
    
    items.forEach(item => {
        const itemElement = createMenuItemElement(item);
        featuredItemsContainer.appendChild(itemElement);
    });
}

// Sample menu items
function getSampleMenuItems() {
    return [
        {
            id: 1,
            name: 'Margherita Pizza',
            description: 'Classic pizza with tomato sauce, mozzarella, and basil',
            price: 12.99,
            category: 'Pizza',
            imageUrl: 'images/menu/margherita.jpg',
            available: true,
            featured: true
        },
        {
            id: 2,
            name: 'Pepperoni Pizza',
            description: 'Pizza with tomato sauce, mozzarella, and pepperoni',
            price: 14.99,
            category: 'Pizza',
            imageUrl: 'images/menu/pepperoni.jpg',
            available: true,
            featured: false
        },
        {
            id: 3,
            name: 'Vegetarian Pizza',
            description: 'Pizza with tomato sauce, mozzarella, and assorted vegetables',
            price: 13.99,
            category: 'Pizza',
            imageUrl: 'images/menu/vegetarian.jpg',
            available: true,
            featured: false
        },
        {
            id: 4,
            name: 'Caesar Salad',
            description: 'Romaine lettuce, croutons, parmesan cheese, and Caesar dressing',
            price: 8.99,
            category: 'Salad',
            imageUrl: 'images/menu/caesar.jpg',
            available: true,
            featured: true
        },
        {
            id: 5,
            name: 'Greek Salad',
            description: 'Tomatoes, cucumbers, onions, feta cheese, and olives',
            price: 9.99,
            category: 'Salad',
            imageUrl: 'images/menu/greek.jpg',
            available: true,
            featured: false
        },
        {
            id: 6,
            name: 'Caprese Salad',
            description: 'Tomatoes, mozzarella, basil, and balsamic glaze',
            price: 10.99,
            category: 'Salad',
            imageUrl: 'images/menu/caprese.jpg',
            available: true,
            featured: false
        },
        {
            id: 7,
            name: 'Spaghetti Bolognese',
            description: 'Spaghetti with meat sauce',
            price: 14.99,
            category: 'Pasta',
            imageUrl: 'images/menu/bolognese.jpg',
            available: true,
            featured: true
        },
        {
            id: 8,
            name: 'Fettuccine Alfredo',
            description: 'Fettuccine pasta with creamy Alfredo sauce',
            price: 13.99,
            category: 'Pasta',
            imageUrl: 'images/menu/alfredo.jpg',
            available: true,
            featured: false
        },
        {
            id: 9,
            name: 'Lasagna',
            description: 'Layers of pasta, meat sauce, and cheese',
            price: 15.99,
            category: 'Pasta',
            imageUrl: 'images/menu/lasagna.jpg',
            available: true,
            featured: false
        },
        {
            id: 10,
            name: 'Grilled Chicken',
            description: 'Grilled chicken breast with vegetables and mashed potatoes',
            price: 16.99,
            category: 'Main Course',
            imageUrl: 'images/menu/grilled_chicken.jpg',
            available: true,
            featured: true
        },
        {
            id: 11,
            name: 'Steak',
            description: 'Grilled steak with vegetables and roasted potatoes',
            price: 19.99,
            category: 'Main Course',
            imageUrl: 'images/menu/steak.jpg',
            available: true,
            featured: false
        },
        {
            id: 12,
            name: 'Salmon',
            description: 'Grilled salmon with vegetables and rice',
            price: 18.99,
            category: 'Main Course',
            imageUrl: 'images/menu/salmon.jpg',
            available: true,
            featured: false
        },
        {
            id: 13,
            name: 'Tiramisu',
            description: 'Italian dessert with coffee-soaked ladyfingers and mascarpone cream',
            price: 7.99,
            category: 'Dessert',
            imageUrl: 'images/menu/tiramisu.jpg',
            available: true,
            featured: false
        },
        {
            id: 14,
            name: 'Cheesecake',
            description: 'Creamy cheesecake with berry compote',
            price: 6.99,
            category: 'Dessert',
            imageUrl: 'images/menu/cheesecake.jpg',
            available: true,
            featured: false
        },
        {
            id: 15,
            name: 'Chocolate Cake',
            description: 'Rich chocolate cake with chocolate ganache',
            price: 6.99,
            category: 'Dessert',
            imageUrl: 'images/menu/chocolate_cake.jpg',
            available: true,
            featured: false
        },
        {
            id: 16,
            name: 'Coca-Cola',
            description: 'Classic Coca-Cola',
            price: 2.99,
            category: 'Beverage',
            imageUrl: 'images/menu/coke.jpg',
            available: true,
            featured: false
        },
        {
            id: 17,
            name: 'Sprite',
            description: 'Refreshing lemon-lime soda',
            price: 2.99,
            category: 'Beverage',
            imageUrl: 'images/menu/sprite.jpg',
            available: true,
            featured: false
        },
        {
            id: 18,
            name: 'Iced Tea',
            description: 'Freshly brewed iced tea',
            price: 2.99,
            category: 'Beverage',
            imageUrl: 'images/menu/iced_tea.jpg',
            available: true,
            featured: false
        }
    ];
}

// Sample featured items
function getSampleFeaturedItems() {
    const allItems = getSampleMenuItems();
    return allItems.filter(item => item.featured);
}
