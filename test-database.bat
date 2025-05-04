@echo off
echo Testing database connection and admin user...
echo.

mvn compile exec:java -Dexec.mainClass="com.bistro.util.DatabaseTest"
echo.
echo If you see any errors, make sure:
echo 1. MySQL is running
echo 2. The database 'bistro_db' exists
echo 3. The user 'bistro_user' with password 'bistro_password' has access to the database
echo.
echo You can run the database setup script with:
echo mysql -u root -p -e "source src/main/resources/db/bistro_db.sql"
echo.
pause
