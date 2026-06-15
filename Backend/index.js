const express = require('express');
const cors = require('cors');
require('dotenv').config();
const { initializeDatabase } = require('./config/db');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
const authRouter = require('./routes/auth');
const attendanceRouter = require('./routes/attendance');

app.use('/api/auth', authRouter);
app.use('/api', attendanceRouter);

// Basic Route for Server Status check
app.get('/health', (req, res) => {
  res.json({ success: true, message: 'Server is running and healthy' });
});

// Setup Port
const PORT = process.env.PORT || 3000;

// Initialize Database, then start Server
async function startServer() {
  console.log('Initializing database connection...');
  await initializeDatabase();
  
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Attendance Management Backend server running on http://localhost:${PORT}`);
  });
}

startServer();
