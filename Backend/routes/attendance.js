const express = require('express');
const router = express.Router();
const db = require('../config/db');

// @route   POST api/sessions
// @desc    Create a new attendance session (Teacher only)
router.post('/sessions', async (req, res) => {
  const { date, session_code } = req.body;

  if (!date || !session_code) {
    return res.status(400).json({ success: false, message: 'Please provide both date and session code' });
  }

  try {
    // 1. Close any existing active sessions
    await db.query("UPDATE attendance_sessions SET status = 'closed' WHERE status = 'active'");

    // 2. Create the new session
    const [sessionResult] = await db.query(
      'INSERT INTO attendance_sessions (date, session_code, status) VALUES (?, ?, ?)',
      [date, session_code, 'active']
    );

    const sessionId = sessionResult.insertId;

    // 3. Automatically populate attendance_records as 'absent' for all active students
    const [students] = await db.query("SELECT id FROM users WHERE role = 'student'");
    
    if (students.length > 0) {
      const recordValues = students.map(student => [sessionId, student.id, 'absent']);
      // Bulk insert
      await db.query(
        'INSERT INTO attendance_records (session_id, student_id, status) VALUES ?',
        [recordValues]
      );
    }

    return res.status(201).json({
      success: true,
      message: 'Attendance session created successfully',
      session: {
        id: sessionId,
        date,
        session_code,
        status: 'active'
      }
    });
  } catch (error) {
    console.error('Create session error:', error);
    return res.status(500).json({ success: false, message: 'Server error while creating session' });
  }
});

// @route   GET api/sessions/active
// @desc    Get the currently active attendance session
router.get('/sessions/active', async (req, res) => {
  try {
    const [sessions] = await db.query("SELECT * FROM attendance_sessions WHERE status = 'active' LIMIT 1");
    if (sessions.length === 0) {
      return res.json({ success: true, session: null });
    }
    return res.json({ success: true, session: sessions[0] });
  } catch (error) {
    console.error('Get active session error:', error);
    return res.status(500).json({ success: false, message: 'Server error retrieving active session' });
  }
});

// @route   POST api/sessions/:id/close
// @desc    Close an active attendance session (Teacher only)
router.post('/sessions/:id/close', async (req, res) => {
  const sessionId = req.params.id;
  try {
    await db.query("UPDATE attendance_sessions SET status = 'closed' WHERE id = ?", [sessionId]);
    return res.json({ success: true, message: 'Session closed successfully' });
  } catch (error) {
    console.error('Close session error:', error);
    return res.status(500).json({ success: false, message: 'Server error closing session' });
  }
});

// @route   GET api/teacher/attendance-summary
// @desc    Get list of all students and their overall attendance percentage (Teacher view)
router.get('/teacher/attendance-summary', async (req, res) => {
  try {
    const queryStr = `
      SELECT 
        u.id, 
        u.name, 
        u.roll_number,
        COUNT(r.id) as total_sessions,
        SUM(CASE WHEN r.status = 'present' THEN 1 ELSE 0 END) as attended_sessions
      FROM users u
      LEFT JOIN attendance_records r ON u.id = r.student_id
      WHERE u.role = 'student'
      GROUP BY u.id, u.name, u.roll_number
      ORDER BY u.name ASC;
    `;
    
    const [results] = await db.query(queryStr);
    
    const studentsSummary = results.map(row => {
      const total = parseInt(row.total_sessions);
      const attended = parseInt(row.attended_sessions || 0);
      const percentage = total > 0 ? parseFloat(((attended / total) * 100).toFixed(1)) : 100.0;
      
      return {
        id: row.id,
        name: row.name,
        roll_number: row.roll_number,
        total_sessions: total,
        attended_sessions: attended,
        attendance_percentage: percentage
      };
    });

    return res.json({ success: true, students: studentsSummary });
  } catch (error) {
    console.error('Get attendance summary error:', error);
    return res.status(500).json({ success: false, message: 'Server error retrieving attendance summary' });
  }
});

// @route   GET api/teacher/student/:id
// @desc    Get detailed attendance history for a single student (Teacher view)
router.get('/teacher/student/:id', async (req, res) => {
  const studentId = req.params.id;
  try {
    // Get student info
    const [students] = await db.query('SELECT name, roll_number FROM users WHERE id = ? AND role = "student"', [studentId]);
    if (students.length === 0) {
      return res.status(404).json({ success: false, message: 'Student not found' });
    }

    const studentInfo = students[0];

    // Get attendance list
    const queryStr = `
      SELECT 
        s.id as session_id,
        s.date,
        s.session_code,
        COALESCE(r.status, 'absent') as status,
        r.submitted_at
      FROM attendance_sessions s
      LEFT JOIN attendance_records r ON s.id = r.session_id AND r.student_id = ?
      ORDER BY s.date DESC, s.id DESC;
    `;
    
    const [attendance] = await db.query(queryStr);

    return res.json({
      success: true,
      student: studentInfo,
      attendance: attendance
    });
  } catch (error) {
    console.error('Get student detail error:', error);
    return res.status(500).json({ success: false, message: 'Server error retrieving student details' });
  }
});

// @route   GET api/student/attendance/:studentId
// @desc    Get attendance summary & history for a single student (Student self-view)
router.get('/student/attendance/:studentId', async (req, res) => {
  const studentId = req.params.studentId;
  try {
    // 1. Calculate overall stats
    const statsQuery = `
      SELECT 
        COUNT(id) as total_sessions,
        SUM(CASE WHEN status = 'present' THEN 1 ELSE 0 END) as attended_sessions
      FROM attendance_records
      WHERE student_id = ?;
    `;
    const [statsResult] = await db.query(statsQuery, [studentId]);
    const total = parseInt(statsResult[0].total_sessions || 0);
    const attended = parseInt(statsResult[0].attended_sessions || 0);
    const percentage = total > 0 ? parseFloat(((attended / total) * 100).toFixed(1)) : 100.0;

    // 2. Retrieve history logs
    const historyQuery = `
      SELECT 
        s.id as session_id,
        s.date,
        s.session_code,
        r.status,
        r.submitted_at
      FROM attendance_records r
      JOIN attendance_sessions s ON r.session_id = s.id
      WHERE r.student_id = ?
      ORDER BY s.date DESC, s.id DESC;
    `;
    const [history] = await db.query(historyQuery, [studentId]);

    return res.json({
      success: true,
      summary: {
        total_sessions: total,
        attended_sessions: attended,
        attendance_percentage: percentage
      },
      history: history
    });
  } catch (error) {
    console.error('Get student attendance history error:', error);
    return res.status(500).json({ success: false, message: 'Server error retrieving student history' });
  }
});

// @route   POST api/student/submit-attendance
// @desc    Submit 4-digit code to mark presence in active session
router.post('/student/submit-attendance', async (req, res) => {
  const { studentId, session_code } = req.body;

  if (!studentId || !session_code) {
    return res.status(400).json({ success: false, message: 'Please provide both student ID and session code' });
  }

  try {
    // 1. Find the active session
    const [activeSessions] = await db.query("SELECT * FROM attendance_sessions WHERE status = 'active' LIMIT 1");
    if (activeSessions.length === 0) {
      return res.status(400).json({ success: false, message: 'No active attendance session found' });
    }

    const activeSession = activeSessions[0];

    // 2. Validate session code
    if (activeSession.session_code !== session_code.trim()) {
      return res.status(400).json({ success: false, message: 'Invalid session code. Please verify the code with your teacher.' });
    }

    // 3. Check if record exists
    const [records] = await db.query(
      'SELECT id, status FROM attendance_records WHERE session_id = ? AND student_id = ?',
      [activeSession.id, studentId]
    );

    if (records.length === 0) {
      // Create record if somehow missing (failsafe)
      await db.query(
        'INSERT INTO attendance_records (session_id, student_id, status, submitted_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)',
        [activeSession.id, studentId, 'present']
      );
    } else {
      const record = records[0];
      if (record.status === 'present') {
        return res.json({ success: true, message: 'Attendance already submitted. You are marked Present.' });
      }

      // Update record to present
      await db.query(
        'UPDATE attendance_records SET status = ?, submitted_at = CURRENT_TIMESTAMP WHERE id = ?',
        ['present', record.id]
      );
    }

    return res.json({ success: true, message: 'Attendance submitted successfully! You are marked Present.' });
  } catch (error) {
    console.error('Submit attendance error:', error);
    return res.status(500).json({ success: false, message: 'Server error during attendance submission' });
  }
});

module.exports = router;
