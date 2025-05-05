CREATE DATABASE  IF NOT EXISTS `bistro_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `bistro_db`;
-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: bistro_db
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cart_id` int NOT NULL,
  `menu_item_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_item` (`cart_id`,`menu_item_id`),
  KEY `menu_item_id` (`menu_item_id`),
  CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,1,1,2,'2025-05-04 14:42:35','2025-05-04 14:48:14'),(3,2,1,1,'2025-05-04 15:00:51','2025-05-04 15:00:51'),(4,3,1,1,'2025-05-04 15:05:21','2025-05-04 15:05:21'),(5,4,2,1,'2025-05-04 15:06:26','2025-05-04 15:06:26'),(6,5,4,1,'2025-05-04 15:09:51','2025-05-04 15:09:51'),(7,6,1,1,'2025-05-04 15:15:50','2025-05-04 15:15:50');
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `session_id` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `carts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,NULL,'3215740DCA2F4D92C7AD1A1D6BC5AA39','2025-05-04 14:42:29','2025-05-04 14:48:14'),(2,NULL,'71A8EEF697C6B5A0AFF7A3F295FEF6A3','2025-05-04 15:00:35','2025-05-04 15:00:51'),(3,NULL,'77D145482200B5E7681BAAE0391D567A','2025-05-04 15:05:16','2025-05-04 15:05:21'),(4,NULL,'5DD5050CFC8D06216D14DCD40BEB7635','2025-05-04 15:06:20','2025-05-04 15:06:26'),(5,NULL,'CB745473DA486D1B8996E9937E9E3966','2025-05-04 15:09:45','2025-05-04 15:09:51'),(6,NULL,'D8839956ABFAA8D0EA89024FBAC03994','2025-05-04 15:15:37','2025-05-04 15:15:50'),(7,NULL,'FEBB824E4CDB1AEC1CC3CC948E75988C','2025-05-04 15:24:51','2025-05-04 15:24:51'),(8,1,'8E45A9AC4D829380BC4B2D7BBD4296C0','2025-05-04 15:32:43','2025-05-05 14:56:23'),(9,NULL,'EFE0563D354AC6198B2B763F4FFA4D1C','2025-05-04 17:53:54','2025-05-04 17:53:54'),(10,NULL,'2081335999608D598751BE213D38A0F0','2025-05-04 17:58:46','2025-05-04 17:58:46'),(11,NULL,'2444FC165EFA04645102A76E263F3FF3','2025-05-04 17:58:59','2025-05-04 17:58:59'),(12,NULL,'963B4389E870312BEE1CBDAFF722E837','2025-05-04 17:59:17','2025-05-04 17:59:17'),(13,NULL,'D4E7B7C990DA533323F84273462AAE7D','2025-05-04 19:43:14','2025-05-04 19:43:14'),(14,NULL,'8645B57A4017E6018794669979C14749','2025-05-04 20:30:23','2025-05-04 20:30:23'),(15,NULL,'B3C8601300BEC1A1DF15E50F63514503','2025-05-04 20:32:33','2025-05-04 20:32:33'),(16,NULL,'79B70EE7A95644840A6F1C7D4E3EEB2D','2025-05-04 20:46:29','2025-05-04 20:46:29'),(17,4,'202DEA0317B9F72ABCF0C599A447EDE6','2025-05-04 21:01:04','2025-05-05 14:24:32'),(18,NULL,'B24B99CC4FAD20A44C61E7DDF7BBF34B','2025-05-04 21:02:12','2025-05-04 21:02:12'),(19,NULL,'46A637280180BB80FA506E1241E68D07','2025-05-04 21:30:50','2025-05-04 21:30:50'),(20,NULL,'5B2BC2A5615E0C6F5A95D7275330DE1C','2025-05-04 21:30:50','2025-05-04 21:30:50'),(21,NULL,'34B19E816C052019E9238D80EB16E06F','2025-05-04 21:30:50','2025-05-04 21:30:50'),(22,9,'38D43DDC0AF2154FB355E2BF3260DAC0','2025-05-04 21:57:32','2025-05-04 21:59:28'),(23,10,'38D43DDC0AF2154FB355E2BF3260DAC0','2025-05-04 21:59:11','2025-05-04 21:59:23'),(24,NULL,'E322F791B41F57D28A017876D6321111','2025-05-05 14:19:36','2025-05-05 14:19:36'),(25,12,'DA64D4BCFEA2E8F37AFBAEAF75174E7C','2025-05-05 14:19:36','2025-05-05 14:25:16');
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_items`
--

DROP TABLE IF EXISTS `menu_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `price` decimal(10,2) NOT NULL,
  `category` varchar(50) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `available` tinyint(1) DEFAULT '1',
  `featured` tinyint(1) DEFAULT '0',
  `preparation_time` int DEFAULT '15',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_items`
--

LOCK TABLES `menu_items` WRITE;
/*!40000 ALTER TABLE `menu_items` DISABLE KEYS */;
INSERT INTO `menu_items` VALUES (1,'Margherita Pizza','Classic pizza with tomato sauce, mozzarella, and basil',299.00,'Pizza','images/menu/margherita.jpg',1,1,0),(2,'Pepperoni Pizza','Pizza with tomato sauce, mozzarella, and pepperoni',249.00,'Pizza','images/menu/pepperoni.jpg',1,1,0),(3,'Vegetarian Pizza','Pizza with tomato sauce, mozzarella, and assorted vegetables',199.00,'Pizza','images/menu/vegetarian.jpg',1,1,0),(4,'Caesar Salad','Romaine lettuce, croutons, parmesan cheese, and Caesar dressing',149.00,'Salad','images/menu/caesar.jpg',1,1,0),(5,'Greek Salad','Mixed greens, feta cheese, olives, tomatoes, and Greek dressing',179.00,'Salad','images/menu/greek.jpg',1,1,0),(6,'Garden Salad','Mixed greens, tomatoes, cucumbers, and balsamic dressing',129.00,'Salad','images/menu/garden.jpg',1,1,0),(7,'Spaghetti Bolognese','Spaghetti with meat sauce',349.00,'Pasta','images/menu/bolognese.jpg',1,1,0),(8,'Fettuccine Alfredo','Fettuccine with creamy Alfredo sauce',349.00,'Pasta','images/menu/alfredo.jpg',1,1,0),(9,'Lasagna','Layered pasta with meat sauce and cheese',399.00,'Pasta','images/menu/lasagna.jpg',1,1,0),(10,'Grilled Chicken','Grilled chicken breast with vegetables and mashed potatoes',449.00,'Main Course','images/menu/grilled_chicken.jpg',1,1,0),(11,'Beef Steak','Grilled beef steak with vegetables and fries',499.00,'Main Course','images/menu/beef_steak.jpg',1,1,0),(12,'Salmon Fillet','Grilled salmon fillet with vegetables and rice',599.00,'Main Course','images/menu/salmon.jpg',1,1,0),(13,'Chocolate Cake','Rich chocolate cake with chocolate frosting',89.00,'Dessert','images/menu/chocolate_cake.jpg',1,1,0),(14,'Cheesecake','Creamy cheesecake with strawberry topping',89.00,'Dessert','images/menu/cheesecake.jpg',1,1,0),(15,'Ice Cream','Vanilla ice cream with chocolate sauce',59.00,'Dessert','images/menu/ice_cream.jpg',1,1,0),(16,'Coca-Cola','Classic Coca-Cola',29.00,'Beverage','images/menu/coke.jpg',1,1,0),(17,'Sprite','Refreshing Sprite',29.00,'Beverage','images/menu/sprite.jpg',1,1,0);
/*!40000 ALTER TABLE `menu_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `menu_item_id` int NOT NULL,
  `menu_item_name` varchar(100) NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `special_instructions` text,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `menu_item_id` (`menu_item_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `status` varchar(20) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `delivery_date` timestamp NULL DEFAULT NULL,
  `delivery_address` text,
  `payment_method` varchar(50) NOT NULL,
  `payment_status` varchar(20) NOT NULL,
  `special_instructions` text,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'CUSTOMER',
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'kishore','kishore@gmail.com','$2a$10$98KSTYPSiNRK/Of8GJ1mceKymWHO1ZNAOVL/ABYwEOPW9G.xr6e9W','ADMIN',NULL,NULL,NULL,NULL,'2025-05-04 15:31:23','2025-05-04 21:50:57'),(2,'admin','admin@bistro.com','$2a$10$zKNWtwJ1SyZypV3wcqHqCuhrrP9Gk7imLAFTq0U3TrD.5B.5a/j5W','ADMIN','                     ','User','','','2025-05-04 21:40:58','2025-05-04 16:28:20'),(3,'staff','staff@bistro.com','$2a$10$LK.f6PLj0aDOy/6gGtGYm.hgKAVYUnHlnDktEXoQ/uUR53h4MT46S','STAFF','Staff','User','','','2025-05-04 21:40:58','2025-05-04 16:28:50'),(4,'customer','customer@example.com','$2a$10$ihYqWDqLbxSQRBFkWYmXnOvFlD3pQy6IP1JYUuGLkEUIcvct6hHBG','CUSTOMER','Customer','User','','','2025-05-04 21:40:58','2025-05-04 16:30:05'),(12,'user1','user1@gmail.com','$2a$10$WCYEmnxyYvBKNXfnfG0A/uxrW23vOBcnQt9mlzeICPEB4GvaN6Ea2','CUSTOMER','user','1','9361070035','-','2025-05-05 08:52:23','2025-05-05 08:52:23');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'bistro_db'
--

--
-- Dumping routines for database 'bistro_db'
--
/*!50003 DROP PROCEDURE IF EXISTS `usp_add_to_cart` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_add_to_cart`(IN p_cart_id INT, IN p_menu_item_id INT, IN p_quantity INT)
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_clear_cart` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_clear_cart`(IN p_cart_id INT)
BEGIN
    -- Delete all items from cart
    DELETE FROM cart_items WHERE cart_id = p_cart_id;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_get_cart_items` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_get_cart_items`(IN p_cart_id INT)
BEGIN
    SELECT ci.id, ci.menu_item_id, ci.quantity, 
           mi.name, mi.price, mi.image_url,
           (mi.price * ci.quantity) AS item_total
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = p_cart_id
    ORDER BY ci.created_at;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_get_cart_total` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_get_cart_total`(IN p_cart_id INT)
BEGIN
    SELECT COALESCE(SUM(mi.price * ci.quantity), 0) AS total
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = p_cart_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_get_or_create_cart` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_get_or_create_cart`(IN p_session_id VARCHAR(100), IN p_user_id INT)
BEGIN
    DECLARE v_cart_id INT;
    
    -- Check if cart exists for session
    SELECT id INTO v_cart_id FROM carts 
    WHERE session_id = p_session_id
    LIMIT 1;
    
    -- If no cart exists, create one
    IF v_cart_id IS NULL THEN
        INSERT INTO carts (session_id, user_id) VALUES (p_session_id, p_user_id);
        SET v_cart_id = LAST_INSERT_ID();
    ELSE
        -- Update user_id if provided
        IF p_user_id IS NOT NULL THEN
            UPDATE carts SET user_id = p_user_id WHERE id = v_cart_id;
        END IF;
    END IF;
    
    -- Return the cart ID
    SELECT v_cart_id AS cart_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_place_order` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_place_order`(
    IN p_user_id INT,
    IN p_delivery_address TEXT,
    IN p_payment_method VARCHAR(50),
    IN p_special_instructions TEXT,
    OUT p_order_id INT
)
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    DECLARE v_cart_id INT;
    
    -- Start transaction
    START TRANSACTION;
    
    -- Get the user's cart ID
    SELECT id INTO v_cart_id FROM carts 
    WHERE user_id = p_user_id
    ORDER BY updated_at DESC 
    LIMIT 1;
    
    IF v_cart_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cart not found for user';
        ROLLBACK;
    END IF;
    
    -- Calculate total amount from cart
    CALL usp_get_cart_total(v_cart_id);
    SELECT COALESCE(SUM(mi.price * ci.quantity), 0) INTO v_total
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = v_cart_id;
    
    -- Create order
    INSERT INTO orders (user_id, status, total_amount, order_date, delivery_address, payment_method, payment_status, special_instructions)
    VALUES (p_user_id, 'PENDING', v_total, NOW(), p_delivery_address, p_payment_method, 'PENDING', p_special_instructions);
    
    -- Get the new order ID
    SET p_order_id = LAST_INSERT_ID();
    
    -- Copy cart items to order items
    INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, price, special_instructions)
    SELECT 
        p_order_id, 
        ci.menu_item_id, 
        mi.name, 
        ci.quantity, 
        mi.price,
        NULL  -- Set special instructions if needed
    FROM cart_items ci
    JOIN menu_items mi ON ci.menu_item_id = mi.id
    WHERE ci.cart_id = v_cart_id;
    
    -- Commit the transaction
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_remove_from_cart` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_remove_from_cart`(IN p_cart_id INT, IN p_menu_item_id INT)
BEGIN
    -- Delete item from cart
    DELETE FROM cart_items 
    WHERE cart_id = p_cart_id AND menu_item_id = p_menu_item_id;
    
    -- Update cart timestamp
    UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = p_cart_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `usp_update_cart_item` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_update_cart_item`(IN p_cart_id INT, IN p_menu_item_id INT, IN p_quantity INT)
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
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-05 20:35:53
