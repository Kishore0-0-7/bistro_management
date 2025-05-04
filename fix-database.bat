@echo off
echo Resetting admin password...
mysql -u bistro_user -pbistro_password bistro_db < reset_admin_password.sql
echo Password reset complete!
pause