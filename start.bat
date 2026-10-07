@echo off
echo =======================================================
echo     Axedrobe E-Commerce Order Management System
echo =======================================================
echo.
echo Checking if Docker is running...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Docker is not running. Attempting to start Docker Desktop...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo Waiting for 15 seconds for Docker to initialize...
    timeout /t 15 /nobreak
    
    echo Re-checking Docker status...
    call :CHECK_DOCKER_AGAIN
)

goto :START_SERVICES

:CHECK_DOCKER_AGAIN
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker failed to start or is not installed in the default location.
    echo Please start Docker Desktop manually and try again.
    pause
    exit /b 1
)
exit /b 0

:START_SERVICES

echo.
echo Starting MongoDB, Kafka, all microservices and the frontend...
echo This might take a minute or two on the first run...
echo.

docker compose up -d --build

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Something went wrong while starting the containers.
    pause
    exit /b 1
)

echo.
echo =======================================================
echo  SUCCESS! All services are up and running!
echo =======================================================
echo.
echo  Website: http://localhost:8080
echo.
echo  Swagger UI Links:
echo  - User Service: http://localhost:8081/swagger-ui.html
echo  - Product Service: http://localhost:8082/swagger-ui.html
echo  - Order Service: http://localhost:8083/swagger-ui.html
echo.

:MENU
echo =======================================================
echo  What would you like to do next?
echo =======================================================
echo  1. Stop all services (Shutdown)
echo  2. Restart all services (Reboot)
echo  3. Exit this terminal (Services will keep running in background)
echo.
set /p choice=Enter your choice (1-3): 

if "%choice%"=="1" goto STOP
if "%choice%"=="2" goto RESTART
if "%choice%"=="3" goto END
goto MENU

:STOP
echo Stopping all services...
docker compose down
echo Services stopped successfully.
pause
exit /b 0

:RESTART
echo Restarting all services...
docker compose restart
echo Services restarted successfully.
goto MENU

:END
exit /b 0
