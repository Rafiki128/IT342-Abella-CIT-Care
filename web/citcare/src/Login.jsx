import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Login.css';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [message, setMessage] = useState({ text: '', type: '' });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // 1. Existing Manual Login Handler
    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8080/api/v1/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });
            const data = await response.json();

            if (response.ok && data.success) {
                localStorage.setItem('accessToken', data.data.accessToken);
                localStorage.setItem('user', JSON.stringify(data.data.user));
                
                setMessage({ text: `Welcome back, ${data.data.user.fullName}! Redirecting...`, type: 'success' });
                setTimeout(() => navigate('/'), 1000);
            } else {
                setMessage({ text: data.error?.message || 'Invalid credentials.', type: 'error' });
            }
        } catch (error) {
            setMessage({ text: 'Server error. Make sure the backend is running.', type: 'error' });
        }
    };

    // 2. New Google Login Handler
    const handleGoogleLogin = () => {
        // Redirects the browser directly to your Spring Boot Google endpoint
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    };

    return (
        <div className="login-wrapper">
            <div className="login-banner">
                <h1>CIT-Care</h1>
                <p>Your centralized campus appointment and clinic management system.</p>
            </div>
            
            <div className="login-content">
                <div className="login-header">
                    <h2>Welcome Back</h2>
                    <p>Please enter your details to sign in.</p>
                </div>
                
                {message.text && (
                    <div className={`message ${message.type}`}>{message.text}</div>
                )}
                
                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter your CIT email" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} placeholder="••••••••" required />
                    </div>
                    <button type="submit" className="btn-primary">Sign In</button>
                </form>
                
                {/* 3. Added Divider and Google Button */}
                <div className="divider" style={{ margin: '20px 0', textAlign: 'center', color: '#666', fontSize: '0.9rem' }}>
                    <span>— OR —</span>
                </div>

                <button 
                    type="button" 
                    onClick={handleGoogleLogin} 
                    className="btn-outline" 
                    style={{ width: '100%', borderColor: '#DB4437', color: '#DB4437', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px', padding: '10px', background: 'white', cursor: 'pointer', borderRadius: '4px', fontWeight: 'bold' }}>
                    <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google Logo" width="20" />
                    Sign in with Google
                </button>
                
                <p className="toggle-link" style={{ marginTop: '20px' }}>
                    Don't have an account? <Link to="/register">Create one here</Link>
                </p>
            </div>
        </div>
    );
};

export default Login;