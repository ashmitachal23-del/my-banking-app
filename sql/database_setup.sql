-- ============================================================
--  SECUREBANK — Run this in MySQL before starting the app
-- ============================================================
CREATE DATABASE IF NOT EXISTS banking_system;
USE banking_system;

-- Users
CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       ENUM('ADMIN','CUSTOMER','STAFF') NOT NULL,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(100),
    phone      VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN DEFAULT TRUE
);

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
    account_id     INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id        INT NOT NULL,
    account_type   ENUM('SAVINGS','CURRENT','FIXED_DEPOSIT') NOT NULL,
    balance        DECIMAL(15,2) DEFAULT 0.00,
    opened_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status         ENUM('ACTIVE','INACTIVE','CLOSED','FROZEN') DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_id       INT NOT NULL,
    transaction_type ENUM('DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
    amount           DECIMAL(15,2) NOT NULL,
    balance_after    DECIMAL(15,2) NOT NULL,
    description      VARCHAR(255),
    ref_account_id   INT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by     INT,
    FOREIGN KEY (account_id)   REFERENCES accounts(account_id),
    FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

-- Loans
CREATE TABLE IF NOT EXISTS loans (
    loan_id       INT AUTO_INCREMENT PRIMARY KEY,
    account_id    INT NOT NULL,
    loan_type     ENUM('HOME','CAR','PERSONAL','EDUCATION','BUSINESS') NOT NULL,
    principal     DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2)  NOT NULL,
    tenure_months INT NOT NULL,
    emi_amount    DECIMAL(15,2) NOT NULL,
    outstanding   DECIMAL(15,2) NOT NULL,
    status        ENUM('PENDING','APPROVED','REJECTED','ACTIVE','CLOSED') DEFAULT 'PENDING',
    applied_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by   INT,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- Loan Payments
CREATE TABLE IF NOT EXISTS loan_payments (
    payment_id   INT AUTO_INCREMENT PRIMARY KEY,
    loan_id      INT NOT NULL,
    amount_paid  DECIMAL(15,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
);

-- ── Default seed data ──────────────────────────────────────
INSERT IGNORE INTO users (username,password,role,full_name,email,phone)
VALUES ('admin','admin123','ADMIN','System Administrator','admin@bank.com','9999999999');

INSERT IGNORE INTO users (username,password,role,full_name,email,phone)
VALUES ('staff1','staff123','STAFF','Bank Staff One','staff1@bank.com','9888888888');

INSERT IGNORE INTO users (username,password,role,full_name,email,phone)
VALUES ('customer1','cust123','CUSTOMER','John Doe','john@email.com','9777777777');

INSERT IGNORE INTO accounts (account_number,user_id,account_type,balance)
SELECT 'ACC0000001', user_id, 'SAVINGS', 50000.00
FROM users WHERE username = 'customer1';
