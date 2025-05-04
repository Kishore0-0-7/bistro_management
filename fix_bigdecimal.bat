@echo off
cd src\main\java
findstr /s /i "double" *.java
echo.
echo Files with errors:
echo 1. src\main\java\com\bistro\controller\CartController.java (line 125)
echo 2. src\main\java\com\bistro\dao\impl\MenuItemDAOImpl.java (lines 30, 69, 270)
echo.
echo Compilation errors:
echo - incompatible types: double cannot be converted to java.math.BigDecimal
echo - incompatible types: java.math.BigDecimal cannot be converted to double 