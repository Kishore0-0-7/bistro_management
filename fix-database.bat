@echo off
echo Fixing database setup...
echo.

cd %~dp0
call mvn clean compile dependency:copy-dependencies
echo.
echo Running database test...
echo.

java -cp target/classes;target/dependency/* com.bistro.util.DatabaseTest

echo.
echo If you see any errors, make sure:
echo 1. MySQL is running
echo 2. The database 'bistro_db' exists
echo 3. The user 'bistro_user' with password 'bistro_password' has access to the database
echo.
pause
