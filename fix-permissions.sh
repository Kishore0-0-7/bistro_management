#!/bin/bash
echo "Fixing file permissions..."

# Make shell scripts executable
chmod +x *.sh

# Ensure database SQL script is readable
chmod 644 src/main/resources/db/bistro_db.sql

# Fix directory permissions
find . -type d -exec chmod 755 {} \;

# Fix file permissions for source files
find ./src -type f -name "*.java" -exec chmod 644 {} \;
find ./src -type f -name "*.html" -exec chmod 644 {} \;
find ./src -type f -name "*.css" -exec chmod 644 {} \;
find ./src -type f -name "*.js" -exec chmod 644 {} \;
find ./src -type f -name "*.properties" -exec chmod 644 {} \;

echo "Permissions fixed successfully!" 