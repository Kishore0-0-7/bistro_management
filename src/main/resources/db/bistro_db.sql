-- Database creation and user setup
CREATE DATABASE IF NOT EXISTS bistro_db;
USE bistro_db;

-- Create user with appropriate privileges
CREATE USER IF NOT EXISTS 'bistro_user'@'localhost' IDENTIFIED BY 'bistro_password';
GRANT ALL PRIVILEGES ON bistro_db.* TO 'bistro_user'@'localhost';
FLUSH PRIVILEGES;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- CUSTOMER, STAFF, ADMIN
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create menu_items table
CREATE TABLE menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(255),
    available BOOLEAN DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    preparation_time INT DEFAULT 15 -- in minutes
);

-- Create carts table
CREATE TABLE carts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    session_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create cart_items table
CREATE TABLE cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (cart_id, menu_item_id)
);

-- Create orders table
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, PREPARING, READY, DELIVERED, CANCELLED
    total_amount DECIMAL(10, 2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivery_date TIMESTAMP NULL,
    delivery_address TEXT,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) NOT NULL, -- PENDING, PAID, FAILED
    special_instructions TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create order_items table
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    menu_item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, role, first_name, last_name)
VALUES ('admin', 'admin@bistro.com', '$2a$10$3g4oIfNqX51bvRdTCRwMb.fY7EcNpFko1vz6BKNzpWTZlR0wX9Zra', 'ADMIN', 'Admin', 'User');

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category, image_url, available, featured, preparation_time)
VALUES 
    ('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'Pizza', 'images/menu/margherita.jpg', TRUE, TRUE, 20),
    ('Pepperoni Pizza', 'Pizza with tomato sauce, mozzarella, and pepperoni', 14.99, 'Pizza', 'images/menu/pepperoni.jpg', TRUE, FALSE, 20),
    ('Vegetarian Pizza', 'Pizza with tomato sauce, mozzarella, and assorted vegetables', 13.99, 'Pizza', 'images/menu/vegetarian.jpg', TRUE, FALSE, 25),
    
    ('Caesar Salad', 'Romaine lettuce, croutons, parmesan cheese, and Caesar dressing', 8.99, 'Salad', 'images/menu/caesar.jpg', TRUE, TRUE, 10),
    ('Greek Salad', 'Mixed greens, feta cheese, olives, tomatoes, and Greek dressing', 9.99, 'Salad', 'images/menu/greek.jpg', TRUE, FALSE, 10),
    ('Garden Salad', 'Mixed greens, tomatoes, cucumbers, and balsamic dressing', 7.99, 'Salad', 'images/menu/garden.jpg', TRUE, FALSE, 10),
    
    ('Spaghetti Bolognese', 'Spaghetti with meat sauce', 14.99, 'Pasta', 'images/menu/bolognese.jpg', TRUE, TRUE, 25),
    ('Fettuccine Alfredo', 'Fettuccine with creamy Alfredo sauce', 13.99, 'Pasta', 'images/menu/alfredo.jpg', TRUE, FALSE, 20),
    ('Lasagna', 'Layered pasta with meat sauce and cheese', 15.99, 'Pasta', 'images/menu/lasagna.jpg', TRUE, FALSE, 30),
    
    ('Grilled Chicken', 'Grilled chicken breast with vegetables and mashed potatoes', 16.99, 'Main Course', 'images/menu/grilled_chicken.jpg', TRUE, TRUE, 25),
    ('Beef Steak', 'Grilled beef steak with vegetables and fries', 22.99, 'Main Course', 'images/menu/beef_steak.jpg', TRUE, FALSE, 30),
    ('Salmon Fillet', 'Grilled salmon fillet with vegetables and rice', 19.99, 'Main Course', 'images/menu/salmon.jpg', TRUE, FALSE, 25),
    
    ('Chocolate Cake', 'Rich chocolate cake with chocolate frosting', 6.99, 'Dessert', 'images/menu/chocolate_cake.jpg', TRUE, TRUE, 5),
    ('Cheesecake', 'Creamy cheesecake with strawberry topping', 7.99, 'Dessert', 'images/menu/cheesecake.jpg', TRUE, FALSE, 5),
    ('Ice Cream', 'Vanilla ice cream with chocolate sauce', 4.99, 'Dessert', 'images/menu/ice_cream.jpg', TRUE, FALSE, 5),
    
    ('Coca-Cola', 'Classic Coca-Cola', 2.99, 'Beverage', 'images/menu/coke.jpg', TRUE, FALSE, 2),
    ('Sprite', 'Refreshing Sprite', 2.99, 'Beverage', 'images/menu/sprite.jpg', TRUE, FALSE, 2),
    ('Orange Juice', 'Freshly squeezed orange juice', 3.99, 'Beverage', 'images/menu/orange_juice.jpg', TRUE, FALSE, 5);

-- Create stored procedures for cart operations

-- Procedure to create or get cart
DELIMITER //
CREATE PROCEDURE usp_get_or_create_cart(IN p_session_id VARCHAR(100), IN p_user_id INT)
BEGIN
    DECLARE v_cart_id INT;
    DECLARE v_user_cart_id INT;
    
    -- Check if cart exists for session
    SELECT id INTO v_cart_id FROM carts 
    WHERE session_id = p_session_id
    LIMIT 1;
    
    -- If user ID is provided, check if user already has a cart
    IF p_user_id IS NOT NULL THEN
        SELECT id INTO v_user_cart_id FROM carts 
        WHERE user_id = p_user_id
        ORDER BY updated_at DESC
        LIMIT 1;
    END IF;
    
    -- Decision logic for cart handling
    IF v_cart_id IS NULL AND v_user_cart_id IS NULL THEN
        -- No cart exists for session or user, create a new one
        INSERT INTO carts (session_id, user_id) VALUES (p_session_id, p_user_id);
        SET v_cart_id = LAST_INSERT_ID();
        
    ELSEIF v_cart_id IS NULL AND v_user_cart_id IS NOT NULL THEN
        -- User has a cart but session doesn't, use user's cart and update session
        UPDATE carts SET session_id = p_session_id WHERE id = v_user_cart_id;
        SET v_cart_id = v_user_cart_id;
        
    ELSEIF v_cart_id IS NOT NULL AND v_user_cart_id IS NULL THEN
        -- Session has a cart but user doesn't, update cart with user ID
        IF p_user_id IS NOT NULL THEN
            UPDATE carts SET user_id = p_user_id WHERE id = v_cart_id;
        END IF;
        
    ELSEIF v_cart_id IS NOT NULL AND v_user_cart_id IS NOT NULL AND v_cart_id != v_user_cart_id THEN
        -- Both session and user have different carts, merge them
        
        -- First, update the session cart with the user ID
        UPDATE carts SET user_id = p_user_id WHERE id = v_cart_id;
        
        -- Then, merge items from user cart into session cart
        INSERT INTO cart_items (cart_id, menu_item_id, quantity)
        SELECT v_cart_id, menu_item_id, quantity
        FROM cart_items
        WHERE cart_id = v_user_cart_id
        ON DUPLICATE KEY UPDATE 
            quantity = cart_items.quantity + VALUES(quantity),
            updated_at = CURRENT_TIMESTAMP;
        
        -- Delete the user's original cart items
        DELETE FROM cart_items WHERE cart_id = v_user_cart_id;
        
        -- Delete the user's original cart
        DELETE FROM carts WHERE id = v_user_cart_id;
    END IF;
    
    -- Return the cart ID
    SELECT v_cart_id AS cart_id;
END //
DELIMITER ;

-- Procedure to add item to cart
DELIMITER //
CREATE PROCEDURE usp_add_to_cart(IN p_cart_id INT, IN p_menu_item_id INT, IN p_quantity INT)
BEGIN
    DECLARE v_count INT;
    
    -- Check if item exists in cart
    SELECT COUNT(*) INTO v_count FROM cart_items 
    WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    
    -- If item exists, update quantity
    IF v_count > 0 THEN
        UPDATE cart_items 
        SET quantity = quantity + p_quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    ELSE
        -- Add new item to cart
        INSERT INTO cart_items (cart_id, menu_item_id, quantity)
        VALUES (p_cart_id, p_menu_item_id, p_quantity);
    END IF;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END //
DELIMITER ;

-- Procedure to update cart item quantity
DELIMITER //
CREATE PROCEDURE usp_update_cart_item(IN p_cart_id INT, IN p_menu_item_id INT, IN p_quantity INT)
BEGIN
    -- If quantity is 0 or less, remove item
    IF p_quantity <= 0 THEN
        DELETE FROM cart_items 
        WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    ELSE
        -- Update quantity
        UPDATE cart_items 
        SET quantity = p_quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    END IF;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END //
DELIMITER ;

-- Procedure to remove item from cart
DELIMITER //
CREATE PROCEDURE usp_remove_from_cart(IN p_cart_id INT, IN p_menu_item_id INT)
BEGIN
    -- Delete item from cart
    DELETE FROM cart_items 
    WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END //
DELIMITER ;

-- Procedure to clear cart
DELIMITER //
CREATE PROCEDURE usp_clear_cart(IN p_cart_id INT)
BEGIN
    -- Delete all items from cart
    DELETE FROM cart_items WHERE cart_id = p_cart_id;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END //
DELIMITER ;

-- Procedure to get cart items with details
DELIMITER //
CREATE PROCEDURE usp_get_cart_items(IN p_cart_id INT)
BEGIN
    SELECT ci.id, ci.menu_item_id, ci.quantity, 
           mi.name, mi.price, mi.image_url,
           (mi.price * ci.quantity) AS item_total
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = p_cart_id
    ORDER BY ci.created_at;
END //
DELIMITER ;

-- Procedure to get cart total
DELIMITER //
CREATE PROCEDURE usp_get_cart_total(IN p_cart_id INT)
BEGIN
    SELECT COALESCE(SUM(mi.price * ci.quantity), 0) AS total
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = p_cart_id;
END //
DELIMITER ;
