const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const db = require('../config/db');

// @route   POST api/auth/register
// @desc    Register a teacher or student
router.post('/register', async (req, res) => {
  const { username, password, name, role, roll_number } = req.body;

  if (!username || !password || !name || !role) {
    return res.status(400).json({ success: false, message: 'Please enter all required fields' });
  }

  if (role !== 'teacher' && role !== 'student') {
    return res.status(400).json({ success: false, message: 'Invalid role specified' });
  }

  if (role === 'student' && !roll_number) {
    return res.status(400).json({ success: false, message: 'Roll number is required for students' });
  }

  try {
    // Check if user already exists
    const [existingUsers] = await db.query('SELECT id FROM users WHERE username = ?', [username]);
    if (existingUsers.length > 0) {
      return res.status(400).json({ success: false, message: 'Username is already taken' });
    }

    // Hash password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    // Insert user
    const [result] = await db.query(
      'INSERT INTO users (username, password, name, role, roll_number) VALUES (?, ?, ?, ?, ?)',
      [username, hashedPassword, name, role, role === 'student' ? roll_number : null]
    );

    // Get created user details
    const newUser = {
      id: result.insertId,
      username,
      name,
      role,
      roll_number: role === 'student' ? roll_number : null
    };

    return res.status(201).json({
      success: true,
      message: 'User registered successfully',
      user: newUser
    });
  } catch (error) {
    console.error('Registration error:', error);
    return res.status(500).json({ success: false, message: 'Server error during registration' });
  }
});

// @route   POST api/auth/login
// @desc    Authenticate user and return details
router.post('/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ success: false, message: 'Please provide both username and password' });
  }

  try {
    // Check for user
    const [users] = await db.query('SELECT * FROM users WHERE username = ?', [username]);
    if (users.length === 0) {
      return res.status(400).json({ success: false, message: 'Invalid username or password' });
    }

    const user = users[0];

    // Validate password
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(400).json({ success: false, message: 'Invalid username or password' });
    }

    // Return user details
    return res.json({
      success: true,
      message: 'Login successful',
      user: {
        id: user.id,
        username: user.username,
        name: user.name,
        role: user.role,
        roll_number: user.roll_number
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ success: false, message: 'Server error during login' });
  }
});

// @route   POST api/auth/seed
// @desc    Force seeding of database (in case tables were cleared)
router.post('/seed', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT COUNT(*) as count FROM users');
    if (rows[0].count > 0) {
      return res.json({ success: true, message: 'Database already has users. Seeding bypassed.' });
    }

    // Call seed function from db
    const pool = db.getPool();
    const hashedTeacherPassword = await bcrypt.hash('teacher123', 10);
    const hashedStudentPassword = await bcrypt.hash('student123', 10);

    await pool.query(
      'INSERT INTO users (username, password, name, role) VALUES (?, ?, ?, ?)',
      ['teacher', hashedTeacherPassword, 'Prof. Sarah Jenkins', 'teacher']
    );

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

    return res.json({ success: true, message: 'Database seeded successfully' });
  } catch (error) {
    console.error('Manual seed error:', error);
    return res.status(500).json({ success: false, message: 'Server error during seeding' });
  }
});

module.exports = router;
