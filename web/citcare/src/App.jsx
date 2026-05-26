import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './features/auth/Login';
import Register from './features/auth/Register';
import Home from './features/home/Home';
import AuthSuccess from './features/auth/AuthSuccess';
import Profile from './features/profile/Profile';
import Dashboard from './features/dashboard/Dashboard';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        {/* Set Home as the default landing page */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/auth-success" element={<AuthSuccess />} />
        <Route path="/admin" element={<Navigate to="/dashboard" replace />} />
        <Route path="/staff" element={<Navigate to="/dashboard" replace />} />
                
        {/* If a user types a random URL, send them back to Home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
