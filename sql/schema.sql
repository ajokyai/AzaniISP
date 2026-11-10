CREATE DATABASE IF NOT EXISTS azani_db;
USE azani_db;

CREATE TABLE institution (
  institution_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category ENUM('PRIMARY','JUNIOR','SENIOR','COLLEGE') NOT NULL,
  location VARCHAR(100),
  reg_date DATE NOT NULL,
  bandwidth_id INT NULL,
  status ENUM('REGISTERED','INSTALLED','CONNECTED','DISCONNECTED') DEFAULT 'REGISTERED'
);

CREATE TABLE contact_person (
  contact_id INT AUTO_INCREMENT PRIMARY KEY,
  institution_id INT NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  position VARCHAR(50),
  FOREIGN KEY (institution_id) REFERENCES institution(institution_id)
);

CREATE TABLE bandwidth_package (
  bandwidth_id INT PRIMARY KEY,
  mbps INT NOT NULL,
  monthly_cost DECIMAL(10,2) NOT NULL
);
INSERT INTO bandwidth_package VALUES (1,4,1200),(2,10,2000),(3,20,3500),(4,25,4000),(5,50,7000);

CREATE TABLE lan_band (
  band_id INT PRIMARY KEY,
  min_nodes INT,
  max_nodes INT,
  cost DECIMAL(10,2)
);
INSERT INTO lan_band VALUES (1,2,10,10000),(2,11,20,20000),(3,21,40,30000),(4,41,100,40000);

CREATE TABLE site_survey (
  survey_id INT AUTO_INCREMENT PRIMARY KEY,
  institution_id INT NOT NULL,
  num_users INT,
  pcs_existing INT,
  pcs_to_buy INT DEFAULT 0,
  lan_ready BOOLEAN,
  nodes_to_buy INT DEFAULT 0,
  survey_date DATE,
  FOREIGN KEY (institution_id) REFERENCES institution(institution_id)
);

CREATE TABLE payment (
  payment_id INT AUTO_INCREMENT PRIMARY KEY,
  institution_id INT NOT NULL,
  payment_type ENUM('REGISTRATION','INSTALLATION','PC','LAN','MONTHLY','FINE','RECONNECTION') NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_date DATE NOT NULL,
  bill_month DATE NULL,
  FOREIGN KEY (institution_id) REFERENCES institution(institution_id)
);

CREATE TABLE monthly_bill (
  bill_id INT AUTO_INCREMENT PRIMARY KEY,
  institution_id INT NOT NULL,
  bill_month DATE NOT NULL,
  bandwidth_id INT NOT NULL,
  amount_due DECIMAL(10,2) NOT NULL,
  fine DECIMAL(10,2) DEFAULT 0,
  reconnection_fee DECIMAL(10,2) DEFAULT 0,
  amount_paid DECIMAL(10,2) DEFAULT 0,
  paid_date DATE NULL,
  FOREIGN KEY (institution_id) REFERENCES institution(institution_id)
);