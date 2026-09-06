@echo off
echo Starting MarutiXchange Backend Services...

:: Discovery Service First
start "Discovery" cmd /k "cd D:\MarutiXchange-Backend\discovery-service && mvn spring-boot:run"

echo Waiting 40 seconds for Discovery to start...
timeout /t 40

:: Rules Engine
start "Rules Engine" cmd /k "cd D:\MarutiXchange-Backend\rules-engine-service && mvn spring-boot:run"

echo Waiting 20 seconds...
timeout /t 20

:: All Other Services
start "User Service" cmd /k "cd D:\MarutiXchange-Backend\user-service && mvn spring-boot:run"
start "Car Listing" cmd /k "cd D:\MarutiXchange-Backend\car-listing-service && mvn spring-boot:run"
start "Bidding" cmd /k "cd D:\MarutiXchange-Backend\bidding-service && mvn spring-boot:run"
start "Notification" cmd /k "cd D:\MarutiXchange-Backend\notification-service && mvn spring-boot:run"
start "Payment" cmd /k "cd D:\MarutiXchange-Backend\payment-service && mvn spring-boot:run"
start "Order" cmd /k "cd D:\MarutiXchange-Backend\order-service && mvn spring-boot:run"
start "Test Drive" cmd /k "cd D:\MarutiXchange-Backend\test-drive-service && mvn spring-boot:run"
start "RC Transfer" cmd /k "cd D:\MarutiXchange-Backend\rc-transfer-service && mvn spring-boot:run"
start "Watchlist" cmd /k "cd D:\MarutiXchange-Backend\watchli