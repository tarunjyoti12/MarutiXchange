-- =============================================
-- MarutiXchange — Create all databases
-- Runs automatically when MySQL starts
-- =============================================

CREATE DATABASE IF NOT EXISTS marutixchange_users;
CREATE DATABASE IF NOT EXISTS marutixchange_cars;
CREATE DATABASE IF NOT EXISTS marutixchange_bidding;
CREATE DATABASE IF NOT EXISTS marutixchange_orders;
CREATE DATABASE IF NOT EXISTS marutixchange_payment;
CREATE DATABASE IF NOT EXISTS marutixchange_notifications;
CREATE DATABASE IF NOT EXISTS marutixchange_testdrive;
CREATE DATABASE IF NOT EXISTS marutixchange_rctransfer;
CREATE DATABASE IF NOT EXISTS marutixchange_watchlist;
CREATE DATABASE IF NOT EXISTS marutixchange_db;

-- Grant root full access
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
