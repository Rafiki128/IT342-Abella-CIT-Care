import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './Login';
import Register from './Register';
import Home from './Home';
import AuthSuccess from './components/AuthSuccess';

function App() {
  return (
    <Router>
      <Routes>
        {/* Set Home as the default landing page */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/auth-success" element={<AuthSuccess />} />
                
        {/* If a user types a random URL, send them back to Home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;