import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Register.css';

const Register = () => {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: ''
    });
    const [message, setMessage] = useState({ text: '', type: '' });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/v1/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });
            const data = await response.json();

            if (response.ok && data.success) {
                setMessage({ text: 'Registration successful! Redirecting to login...', type: 'success' });
                setFormData({ fullName: '', email: '', password: '' });
                setTimeout(() => navigate('/login'), 900);
            } else {
                setMessage({ text: data.error?.message || 'Registration failed.', type: 'error' });
            }
        } catch {
            setMessage({ text: 'Server error. Make sure the backend is running.', type: 'error' });
        }
    };

    return (
        <div className="register-wrapper">
            <div className="register-banner">
                <h1>CIT-Care</h1>
                <p>Join the CIT-U campus health and appointment network today.</p>
            </div>
            
            <div className="register-content">
                <div className="register-header">
                    <h2>Create Account</h2>
                    <p>Create a student account. Staff access is assigned by an administrator.</p>
                </div>
                
                {message.text && (
                    <div className={`message ${message.type}`}>{message.text}</div>
                )}
                
                <form onSubmit={handleRegister}>
                    <div className="form-group">
                        <label htmlFor="fullName">Full Name</label>
                        <input type="text" id="fullName" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="Juan Dela Cruz" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} placeholder="juan.delacruz@cit.edu" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} placeholder="Create a strong password" required />
                    </div>
                    <button type="submit" className="btn-primary">Register</button>
                </form>
                
                <p className="toggle-link">Already have an account? <Link to="/login">Sign in here</Link></p>
            </div>
        </div>
    );
};

export default Register;
