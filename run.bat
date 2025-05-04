@echo off
echo Starting Bistro Restaurant Web Application...
echo.
echo This script will run the application using Maven Tomcat plugin.
echo The application will be available at http://localhost:8080/
echo.
echo Press Ctrl+C to stop the application.
echo.

mvn clean tomcat7:run
