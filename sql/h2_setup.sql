-- H2-compatible schema and seed for embedded fallback
-- Removes MySQL-specific syntax (CREATE DATABASE, USE, ENUM, INSERT IGNORE)

CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS accounts (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  account_number VARCHAR(20) NOT NULL UNIQUE,
  user_id INT NOT NULL,
  account_type VARCHAR(20) NOT NULL,
  balance DECIMAL(15,2) DEFAULT 0.00,
  opened_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  status VARCHAR(20) DEFAULT 'ACTIVE',
  CONSTRAINT fk_acc_user FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  transaction_type VARCHAR(30) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  balance_after DECIMAL(15,2) NOT NULL,
  description VARCHAR(255),
  ref_account_id INT,
  transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  performed_by INT,
  CONSTRAINT fk_tr_account FOREIGN KEY(account_id) REFERENCES accounts(account_id),
  CONSTRAINT fk_tr_user FOREIGN KEY(performed_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS loans (
  loan_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  loan_type VARCHAR(20) NOT NULL,
  principal DECIMAL(15,2) NOT NULL,
  interest_rate DECIMAL(5,2) NOT NULL,
  tenure_months INT NOT NULL,
  emi_amount DECIMAL(15,2) NOT NULL,
  outstanding DECIMAL(15,2) NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  approved_by INT,
  CONSTRAINT fk_loan_account FOREIGN KEY(account_id) REFERENCES accounts(account_id)
);

CREATE TABLE IF NOT EXISTS loan_payments (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  loan_id INT NOT NULL,
  amount_paid DECIMAL(15,2) NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  CONSTRAINT fk_payment_loan FOREIGN KEY(loan_id) REFERENCES loans(loan_id)
);

-- Seed users (use MERGE to avoid duplicates)
MERGE INTO users (username, password, role, full_name, email, phone)
KEY(username)
VALUES
('admin','admin123','ADMIN','System Administrator','admin@bank.com','9999999999'),
('staff1','staff123','STAFF','Bank Staff One','staff1@bank.com','9888888888'),
('customer1','cust123','CUSTOMER','John Doe','john@email.com','9777777777');

-- Create a default account for customer1
INSERT INTO accounts (account_number, user_id, account_type, balance)
SELECT 'ACC0000001', user_id, 'SAVINGS', 50000.00 FROM users WHERE username='customer1';
