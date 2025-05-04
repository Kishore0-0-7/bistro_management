# Bistro Restaurant Web Application

A restaurant management web application for Bistro Restaurant using Java backend with MySQL database and a responsive HTML, CSS, and JavaScript frontend.

## Features

- **User Authentication**: Register, login, and logout functionality
- **Menu Management**: Browse, filter, and search menu items
- **Shopping Cart**: Add, update, remove items from cart
- **Order Management**: Place orders, view order history, and order details
- **Admin Dashboard**: Manage menu items, orders, and users (admin only)
- **Responsive Design**: Works on desktop and mobile devices

## Technology Stack

### Backend
- Java Servlets for API endpoints
- MySQL database for data storage
- JDBC for database interactions
- Maven for dependency management
- Tomcat server for deployment

### Frontend
- HTML5 for structure
- CSS3 for styling
- JavaScript for client-side functionality

## Project Structure

```
bistro-restaurant/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bistro/
│   │   │           ├── controller/  # API endpoints
│   │   │           ├── dao/         # Data Access Objects
│   │   │           ├── model/       # Data models
│   │   │           ├── service/     # Business logic
│   │   │           └── util/        # Utility classes
│   │   ├── resources/
│   │   │   └── db/                  # Database scripts
│   │   └── webapp/
│   │       ├── WEB-INF/             # Web configuration
│   │       ├── css/                 # Stylesheets
│   │       ├── js/                  # JavaScript files
│   │       └── images/              # Image assets
└── pom.xml                          # Maven configuration
```

## Setup Instructions

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven
- MySQL Server
- Apache Tomcat 9 or higher

### Database Setup

1. Start MySQL server
2. Run the database setup script:

```bash
# For Linux/Mac
mysql -u root -p < src/main/resources/db/bistro_db.sql

# For Windows
mysql -u root -p -e "source src/main/resources/db/bistro_db.sql"
```

This will:
- Create the `bistro_db` database
- Create a user `bistro_user` with password `bistro_password`
- Create the necessary tables
- Insert sample data

### Running the Application

#### Option 1: Using Maven Tomcat Plugin (Recommended)

1. Navigate to the project directory
2. Run the application using Maven:

```bash
mvn tomcat7:run
```

3. Access the application at `http://localhost:8080/`

#### Option 2: Building and Deploying to External Tomcat

1. Build the project using Maven:

```bash
mvn clean package
```

2. Copy the WAR file from the `target` directory to Tomcat's `webapps` directory
3. Start Tomcat server
4. Access the application at `http://localhost:8080/bistro`

## Default Admin Login

- Username: admin
- Password: admin123

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Register
- `POST /api/auth/logout` - Logout
- `GET /api/auth/check` - Check authentication status

### Menu
- `GET /api/menu` - Get all menu items
- `GET /api/menu/categories` - Get all categories
- `GET /api/menu/featured` - Get featured menu items
- `GET /api/menu/category/{category}` - Get menu items by category
- `GET /api/menu/search?q={query}` - Search menu items
- `GET /api/menu/{id}` - Get menu item by ID
- `POST /api/menu` - Add new menu item (admin only)
- `PUT /api/menu/{id}` - Update menu item (admin only)
- `DELETE /api/menu/{id}` - Delete menu item (admin only)

### Cart
- `GET /api/cart` - Get cart
- `POST /api/cart/add` - Add item to cart
- `POST /api/cart/update` - Update cart item
- `POST /api/cart/remove` - Remove item from cart
- `POST /api/cart/clear` - Clear cart

### Orders
- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Place order
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Cancel order

### Admin
- `GET /api/admin/dashboard` - Get dashboard data
- `GET /api/admin/users` - Get all users

## License

This project is licensed under the MIT License - see the LICENSE file for details.
