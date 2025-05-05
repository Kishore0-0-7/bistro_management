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
- Apache Maven 3.6.0 or higher
- MySQL Server 8.0 or higher
- Apache Tomcat 9 (embedded via Maven plugin)

### Step 1: Clone or Download the Project

Clone the repository or download and extract the ZIP file to your local machine.

### Step 2: Configure Database

1. Start MySQL server
2. Create a database user with appropriate permissions:
   - For Windows:
     ```
     mysql -u root -p
     CREATE USER 'bistro_user'@'localhost' IDENTIFIED BY 'bistro_password';
     GRANT ALL PRIVILEGES ON *.* TO 'bistro_user'@'localhost';
     FLUSH PRIVILEGES;
     exit
     ```
   - For Linux/Mac:
     ```
     mysql -u root -p
     CREATE USER 'bistro_user'@'localhost' IDENTIFIED BY 'bistro_password';
     GRANT ALL PRIVILEGES ON *.* TO 'bistro_user'@'localhost';
     FLUSH PRIVILEGES;
     exit
     ```

3. Run the database setup script:
   - For Windows:
     ```
     mysql -u root -p < src\main\resources\db\bistro_db.sql
     ```
   - For Linux/Mac:
     ```
     mysql -u root -p < src/main/resources/db/bistro_db.sql
     ```

Alternatively, you can use the provided test-database.bat (Windows) or test-database.sh (Unix/Mac) script:
- Windows: `test-database.bat`
- Linux/Mac: `./test-database.sh`

### Step 3: Configure Database Connection (if necessary)

If your MySQL setup uses different credentials or port, modify the database configuration in:
`src/main/java/com/bistro/util/DatabaseConfig.java`

Update the following constants as needed:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/bistro_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String DB_USER = "root"; // Change to your MySQL username
private static final String DB_PASSWORD = "root"; // Change to your MySQL password
```

### Step 4: Run the Application

#### Option 1: Using the provided scripts
- For Windows: Run the `run.bat` file
- For Linux/Mac: Run the `run.sh` file (make it executable first with `chmod +x run.sh`)

#### Option 2: Using Maven directly
1. Open a terminal/command prompt
2. Navigate to the project directory
3. Run the command:
   ```
   mvn clean tomcat7:run
   ```

### Step 5: Access the Application
Open your web browser and go to: http://localhost:8080/

## Default Login Credentials

### Admin User
- Username: admin
- Password: admin123

### Regular User
- Username: user
- Password: user123

## Troubleshooting

### Database Connection Issues
1. Ensure MySQL server is running
2. Verify database credentials in `DatabaseConfig.java`
3. Make sure the database and tables are created properly
4. Run the database test script to verify connection:
   - Windows: `test-database.bat`
   - Linux/Mac: `./test-database.sh`

### Application Not Starting
1. Check if port 8080 is already in use by another application
2. Verify that you have JDK 11 or higher installed
3. Ensure Maven is properly installed and in your PATH

### JavaScript Errors
1. Clear your browser cache
2. Check browser console for specific errors
3. Ensure all JavaScript files are properly loaded

### Cross-Origin (CORS) Issues
If accessing the API from a different domain or port:
1. Modify the CORS filter in `src/main/java/com/bistro/util/CORSFilter.java`
2. Ensure the allowed origins include your frontend domain

## Development Setup

### IDE Configuration
For optimal development experience:
1. Import as Maven project in your IDE
2. Set JDK 11 or higher
3. Set Project encoding to UTF-8

### Hot Reloading
For faster development, enable hot reloading:
1. Add the following to the tomcat7-maven-plugin configuration:
   ```xml
   <contextReloadable>true</contextReloadable>
   ```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
