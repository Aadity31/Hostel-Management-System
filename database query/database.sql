-- -----------------------------------------
-- ✅ Hostel Management System - Full SQL Setup
-- Includes: Tables, Indexes, Auto-Increments, Constraints, Sample Data
-- Author: Aadi
-- -----------------------------------------

-- Set SQL Modes and Transaction
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+05:30";

-- -------------------------------
-- 1. Student Table
-- -------------------------------
CREATE TABLE student (
  Student_id INT PRIMARY KEY,
  First_name VARCHAR(50) NOT NULL,
  Last_name VARCHAR(50) NOT NULL,
  Email VARCHAR(100) NOT NULL UNIQUE,
  Mobile_number VARCHAR(15) NOT NULL UNIQUE,
  Gender VARCHAR(10),
  Study_program VARCHAR(50),
  Password VARCHAR(100) NOT NULL,
  Room_no INT
);

-- -------------------------------
-- 2. Admin Table (Admin / Staff / Student Login Users)
-- -------------------------------
CREATE TABLE admin (
  User_id INT PRIMARY KEY AUTO_INCREMENT,
  Username VARCHAR(20) NOT NULL UNIQUE,
  Password VARCHAR(30) NOT NULL,
  User_type VARCHAR(20) NOT NULL
);

CREATE TABLE staff (
  Staff_id INT PRIMARY KEY AUTO_INCREMENT,
  First_name VARCHAR(50) NOT NULL,
  Last_name VARCHAR(50) NOT NULL,
  Email VARCHAR(100) NOT NULL UNIQUE,
  Mobile_number VARCHAR(15) NOT NULL UNIQUE,
  Gender VARCHAR(10),
  Department VARCHAR(100),
  Designation VARCHAR(100),
  Username VARCHAR(50) NOT NULL UNIQUE,
  Password VARCHAR(100) NOT NULL
);

INSERT INTO staff
(First_name, Last_name, Email, Mobile_number, Gender, Department, Designation, Username, Password)
VALUES
('Ravi', 'Kumar', 'ravi.kumar@example.com', '9876543210', 'Male', 'Maintenance', 'Warden', 'ravi', 'ravi123'),
('Anjali', 'Sharma', 'anjali.sharma@example.com', '9876512345', 'Female', 'Accounts', 'Accountant', 'anjali', 'anjali123');

-- Sample admin data
INSERT INTO admin (User_id, Username, Password, User_type) VALUES
(1, 'Admin', 'admin@123', 'Admin'),
(17, 'a', '1', 'Student'),
(18, 'staff', 'staff', 'Staff');

-- -------------------------------
-- 3. Hostels Table
-- -------------------------------
CREATE TABLE hostels (
  Hostel_ID INT PRIMARY KEY AUTO_INCREMENT,
  Hostel_Name VARCHAR(255) NOT NULL UNIQUE,
  Location VARCHAR(255),
  Total_Rooms INT,
  Available_Rooms INT,
  Hostel_Type VARCHAR(50),
  Contact_Person VARCHAR(255),
  Contact_Number VARCHAR(20),
  Email VARCHAR(255),
  Description TEXT
);

-- -------------------------------
-- 4. Rooms Table
-- -------------------------------
CREATE TABLE rooms (
  Room_ID INT PRIMARY KEY AUTO_INCREMENT,
  Room_Type VARCHAR(30) NOT NULL,
  Capacity INT NOT NULL,
  Current_Occupants INT DEFAULT 0,
  Hostel_ID INT,
  Facility VARCHAR(100) NOT NULL,
  Room_Status VARCHAR(30) NOT NULL,
  Monthly_Rent DECIMAL(10,2),
  FOREIGN KEY (Hostel_ID) REFERENCES hostels(Hostel_ID)
);

-- -------------------------------
-- 5. Hostel Requests Table
-- -------------------------------
CREATE TABLE hostel_requests (
  Request_id INT PRIMARY KEY AUTO_INCREMENT,
  Student_id INT NOT NULL,
  Hostel_id INT NOT NULL,
  Room_id INT NOT NULL,
  Request_date DATE DEFAULT CURRENT_DATE,
  Status VARCHAR(20) DEFAULT 'Pending',
  FOREIGN KEY (Student_id) REFERENCES student(Student_id),
  FOREIGN KEY (Hostel_id) REFERENCES hostels(Hostel_ID),
  FOREIGN KEY (Room_id) REFERENCES rooms(Room_ID)
);

-- -------------------------------
-- 6. Room Allotments Table
-- -------------------------------
CREATE TABLE room_allotments (
  Allotment_id INT PRIMARY KEY AUTO_INCREMENT,
  Student_id INT NOT NULL,
  Room_id INT NOT NULL,
  Hostel_id INT NOT NULL,
  Allotment_date DATE DEFAULT CURRENT_DATE,
  Vacate_date DATE DEFAULT NULL,
  FOREIGN KEY (Student_id) REFERENCES student(Student_id),
  FOREIGN KEY (Room_id) REFERENCES rooms(Room_ID),
  FOREIGN KEY (Hostel_id) REFERENCES hostels(Hostel_ID)
);

-- -------------------------------
-- 7. Fees Table
-- -------------------------------
CREATE TABLE fees (
  Fee_id INT PRIMARY KEY AUTO_INCREMENT,
  Student_id INT NOT NULL,
  Invoice_no VARCHAR(50) NOT NULL UNIQUE,
  Fee_date DATE NOT NULL,
  Fee_month VARCHAR(30) NOT NULL,
  Amount INT NOT NULL
);

-- -------------------------------
-- 8. Logs Table
-- -------------------------------
CREATE TABLE logs (
  Logs_id INT PRIMARY KEY AUTO_INCREMENT,
  User_id INT NOT NULL,
  Login_Date DATETIME DEFAULT CURRENT_TIMESTAMP,
  Status VARCHAR(80) NOT NULL,
  FOREIGN KEY (User_id) REFERENCES admin(User_id)
);

CREATE TABLE IF NOT EXISTS logs (
    logs_id INT AUTO_INCREMENT PRIMARY KEY,
    User_id INT NOT NULL,
    Date VARCHAR(100) NOT NULL,
    Status VARCHAR(255) NOT NULL
);


-- -------------------------------
-- Indexes and Constraints (Expanded)
-- -------------------------------
ALTER TABLE staff ADD COLUMN User_type VARCHAR(50) NOT NULL DEFAULT 'Staff';
ALTER TABLE logs ADD Username VARCHAR(50);
ALTER TABLE fees ADD KEY `INDEX` (Student_id);
ALTER TABLE logs ADD KEY `INDEX` (User_id);
ALTER TABLE student ADD KEY `INDEX` (Room_no);

-- -------------------------------
-- AUTO_INCREMENT Adjustments
-- -------------------------------
ALTER TABLE hostels MODIFY Hostel_ID INT(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
ALTER TABLE logs MODIFY Logs_id INT(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=137;
ALTER TABLE rooms MODIFY Room_ID INT(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;
ALTER TABLE admin MODIFY User_id INT(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

-- -------------------------------
-- Additional Foreign Key Constraints
-- -------------------------------
ALTER TABLE student
  ADD CONSTRAINT student_ibfk_1 FOREIGN KEY (Room_no) REFERENCES rooms (Room_ID) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE fees
  ADD CONSTRAINT fees_ibfk_1 FOREIGN KEY (Student_id) REFERENCES student(Student_id) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;
