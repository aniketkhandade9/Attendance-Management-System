const mysql = require('mysql2/promise');
require('dotenv').config();

const dbConfig = {
  host: process.env.DB_HOST || '127.0.0.1',
  port: process.env.DB_PORT || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASS || '',
};

let pool;

async function initializeDatabase() {
  try {
    // 1. Connect without database to ensure it exists
    const tempConnection = await mysql.createConnection(dbConfig);
    await tempConnection.query('CREATE DATABASE IF NOT EXISTS attendance_db;');
    await tempConnection.end();

    // 2. Setup pool with the database selected
    pool = mysql.createPool({
      ...dbConfig,
      database: 'attendance_db',
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0,
    });

    console.log('Successfully connected to MySQL database: attendance_db');

    // 3. Initialize tables
    await createTables();

    // 4. Seed initial admin/test accounts if users table is empty
    await seedDefaultUsers();

  } catch (error) {
    console.error('----------------------------------------------------');
    console.error('CRITICAL: Failed to connect to MySQL or initialize database.');
    console.error('Please verify that MySQL is running and that credentials in the Backend/.env file are correct.');
    console.error(`Current config - Host: ${dbConfig.host}:${dbConfig.port}, User: ${dbConfig.user}`);
    console.error(`Error details: ${error.message}`);
    console.error('----------------------------------------------------');
    process.exit(1);
  }
}

async function createTables() {
  const usersTable = `
    CREATE TABLE IF NOT EXISTS users (
      id INT AUTO_INCREMENT PRIMARY KEY,
      username VARCHAR(50) UNIQUE NOT NULL,
      password VARCHAR(255) NOT NULL,
      name VARCHAR(100) NOT NULL,
      role ENUM('teacher', 'student') NOT NULL,
      roll_number VARCHAR(50) DEFAULT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
  `;

  const sessionsTable = `
    CREATE TABLE IF NOT EXISTS attendance_sessions (
      id INT AUTO_INCREMENT PRIMARY KEY,
      date DATE NOT NULL,
      session_code VARCHAR(10) NOT NULL,
      status ENUM('active', 'closed') DEFAULT 'active',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;
  `;

  const recordsTable = `
    CREATE TABLE IF NOT EXISTS attendance_records (
      id INT AUTO_INCREMENT PRIMARY KEY,
      session_id INT NOT NULL,
      student_id INT NOT NULL,
      status ENUM('present', 'absent') DEFAULT 'absent',
      submitted_at TIMESTAMP NULL DEFAULT NULL,
      FOREIGN KEY (session_id) REFERENCES attendance_sessions(id) ON DELETE CASCADE,
      FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
      UNIQUE KEY unique_session_student (session_id, student_id)
    ) ENGINE=InnoDB;
  `;

  await pool.query(usersTable);
  await pool.query(sessionsTable);
  await pool.query(recordsTable);
  console.log('Database tables verified/created successfully.');
}

async function seedDefaultUsers() {
  const [rows] = await pool.query('SELECT COUNT(*) as count FROM users');
  if (rows[0].count === 0) {
    const bcrypt = require('bcryptjs');
    console.log('No users found in database. Seeding default accounts...');

    const hashedTeacherPassword = await bcrypt.hash('teacher123', 10);
    const hashedStudentPassword = await bcrypt.hash('student123', 10);

    // Insert 1 teacher
    await pool.query(
      'INSERT INTO users (username, password, name, role) VALUES (?, ?, ?, ?)',
      ['teacher', hashedTeacherPassword, 'Prof. Sarah Jenkins', 'teacher']
    );

    // Insert 3 students
    await pool.query(
      'INSERT INTO users (username, password, name, role, roll_number) VALUES (?, ?, ?, ?, ?)',
      ['student1', hashedStudentPassword, 'Alice Smith', 'student', 'S-2026-001']
    );
    await pool.query(
      'INSERT INTO users (username, password, name, role, roll_number) VALUES (?, ?, ?, ?, ?)',
      ['student2', hashedStudentPassword, 'Bob Jones', 'student', 'S-2026-002']
    );
    await pool.query(
      'INSERT INTO users (username, password, name, role, roll_number) VALUES (?, ?, ?, ?, ?)',
      ['student3', hashedStudentPassword, 'Charlie Brown', 'student', 'S-2026-003']
    );

    console.log('Seeding completed. Default accounts:');
    console.log('  Teacher: username="teacher", password="teacher123"');
    console.log('  Students: username="student1" / "student2" / "student3", password="student123"');
  }
}

module.exports = {
  initializeDatabase,
  query: (text, params) => pool.query(text, params),
  getPool: () => pool
};
