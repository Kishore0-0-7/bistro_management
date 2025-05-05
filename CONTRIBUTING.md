# Contributing to Bistro Restaurant

Thank you for considering contributing to the Bistro Restaurant project! This document provides guidelines and instructions for contributing.

## Development Environment Setup

### Prerequisites
- JDK 11 or higher
- Maven 3.6.0 or higher
- MySQL 8.0 or higher
- Git

### Setup Steps

1. **Clone the repository**
   ```
   git clone https://github.com/yourusername/bistro-restaurant.git
   cd bistro-restaurant
   ```

2. **Set up the database**
   - Start MySQL server
   - Run the database setup script:
     ```
     mysql -u root -p < src/main/resources/db/bistro_db.sql
     ```

3. **Configure database connection (if necessary)**
   - Edit `src/main/resources/db/database.properties` to match your MySQL configuration

4. **Run the application**
   - On Windows: `run.bat`
   - On Linux/Mac: `./run.sh`

## Code Guidelines

### Java

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods small and focused on a single responsibility
- Use proper exception handling

### JavaScript

- Follow standard JavaScript naming conventions
- Use ES6 features when possible, but ensure browser compatibility
- Add function comments for complex functions
- Use proper error handling with try/catch blocks

### HTML/CSS

- Follow HTML5 standards
- Use meaningful class and ID names
- Ensure responsive design works on various screen sizes
- Keep CSS organized and use consistent naming conventions

## Git Workflow

1. **Create a branch for your feature or bugfix**
   ```
   git checkout -b feature/your-feature-name
   ```
   or
   ```
   git checkout -b bugfix/bug-description
   ```

2. **Make changes and commit them**
   ```
   git add .
   git commit -m "Descriptive commit message"
   ```

3. **Push your branch**
   ```
   git push origin feature/your-feature-name
   ```

4. **Create a pull request**
   - Describe what changes you've made
   - Reference any related issues
   - Ensure all tests pass

## Common Issues and Solutions

### Database Connection Issues
If you encounter database connection issues:
1. Verify MySQL is running
2. Check database credentials in `database.properties`
3. Ensure the database has been created with the correct schema

### JavaScript Browser Compatibility
When adding new JavaScript features:
1. Check browser compatibility on [Can I Use](https://caniuse.com/)
2. Add necessary polyfills for older browsers
3. Test on multiple browsers

### Cross-platform Path Issues
To avoid path-related issues:
1. Always use forward slashes (/) in code
2. Use path normalization functions when working with file paths
3. Test file operations on both Windows and Unix-based systems

## Testing

Before submitting a pull request:
1. Test your changes thoroughly
2. Ensure the application works on different browsers
3. Test on both Windows and Unix-based systems if possible

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Help maintain a welcoming environment for all contributors

Thank you for contributing to make Bistro Restaurant better! 