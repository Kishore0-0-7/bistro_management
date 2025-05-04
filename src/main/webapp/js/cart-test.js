// Cart Test Script

// Function to test cart functionality
function testCartFunctionality() {
    console.log('Testing cart functionality...');
    
    // Clear existing cart for testing
    localStorage.removeItem('cart');
    
    // Test item
    const testItem = {
        id: 999,
        name: 'Test Item',
        price: 9.99,
        imageUrl: 'images/menu/default.jpg'
    };
    
    // Test adding to cart
    console.log('Testing addToCart...');
    addToCart(testItem);
    
    // Get cart from local storage
    let cart = JSON.parse(localStorage.getItem('cart')) || [];
    
    // Verify item was added
    console.log('Cart after adding item:', cart);
    const addedItem = cart.find(item => item.id === testItem.id);
    
    if (addedItem) {
        console.log('✅ Item successfully added to cart');
    } else {
        console.error('❌ Failed to add item to cart');
    }
    
    // Verify cart count was updated
    const cartCount = document.getElementById('cart-count');
    if (cartCount && cartCount.textContent === '1') {
        console.log('✅ Cart count updated correctly');
    } else {
        console.error('❌ Cart count not updated correctly');
    }
    
    // Test increasing quantity
    console.log('Testing increaseQuantity...');
    increaseQuantity(testItem.id);
    
    // Get updated cart
    cart = JSON.parse(localStorage.getItem('cart')) || [];
    const updatedItem = cart.find(item => item.id === testItem.id);
    
    if (updatedItem && updatedItem.quantity === 2) {
        console.log('✅ Quantity increased correctly');
    } else {
        console.error('❌ Failed to increase quantity');
    }
    
    // Clear cart after testing
    localStorage.removeItem('cart');
    console.log('Test completed and cart cleared');
}

// Add a simple direct test that can be called from the console
window.testAddToCart = function() {
    console.log('Running manual cart test...');
    
    // Test item
    const testItem = {
        id: 888,
        name: 'Console Test Item',
        price: 19.99,
        imageUrl: 'images/menu/default.jpg'
    };
    
    // Directly call addToCart
    if (typeof addToCart === 'function') {
        addToCart(testItem);
        console.log('Item should be added to cart now');
        console.log('Current cart:', JSON.parse(localStorage.getItem('cart')));
    } else {
        console.error('addToCart function not found!');
    }
};

// Add a function to manually show the cart modal
window.showCartModal = function() {
    console.log('Attempting to show cart modal...');
    if (typeof showModal === 'function') {
        showModal('cart-modal');
    } else {
        console.error('showModal function not found!');
        // Try direct DOM manipulation as fallback
        const modal = document.getElementById('cart-modal');
        if (modal) {
            modal.style.display = 'block';
            console.log('Showed cart modal via direct DOM manipulation');
        } else {
            console.error('Cart modal element not found!');
        }
    }
};

// Add button to run tests
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    addTestButton();
} else {
    document.addEventListener('DOMContentLoaded', addTestButton);
}

function addTestButton() {
    const button = document.createElement('button');
    button.textContent = 'Test Cart';
    button.id = 'test-cart-btn';
    button.style.position = 'fixed';
    button.style.bottom = '10px';
    button.style.right = '10px';
    button.style.zIndex = '9999';
    button.style.padding = '10px';
    button.style.backgroundColor = '#ff6b6b';
    button.style.color = 'white';
    button.style.border = 'none';
    button.style.borderRadius = '4px';
    button.style.cursor = 'pointer';
    
    button.addEventListener('click', testCartFunctionality);
    
    document.body.appendChild(button);
} 