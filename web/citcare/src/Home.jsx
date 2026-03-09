import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    // Check if the user is logged in when the page loads
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const userData = localStorage.getItem('user');
        
        if (token && userData) {
            setUser(JSON.parse(userData));
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        setUser(null); // Instantly updates the screen to show the public landing page
    };

    return (
        <div>
            <nav className="navbar">
                <h2>CIT-Care</h2>
                <div className="nav-actions">
                    {/* If logged in, show name and logout. If not, show login/register links */}
                    {user ? (
                        <>
                            <span className="user-greeting">Hello, {user.fullName}</span>
                            <button onClick={handleLogout} className="logout-btn">Logout</button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="nav-link">Login</Link>
                            <Link to="/register" className="nav-btn">Register</Link>
                        </>
                    )}
                </div>
            </nav>

            <div className="dashboard-container">
                {user ? (
                    // --- WHAT LOGGED-IN USERS SEE (DASHBOARD) ---
                    <>
                        <h1 className="dashboard-header">Welcome to your Dashboard!</h1>
                        <p>This is the main hub where you will be able to book clinic appointments and view your history.</p>
                        
                        <div className="appointments-card">
                            <h3>Upcoming Appointments</h3>
                            <p className="empty-state">No appointments scheduled yet. (Feature coming in Phase 2)</p>
                        </div>
                    </>
                ) : (
                    // --- WHAT GUESTS SEE (LANDING PAGE) ---
                    <div className="landing-content">
                        <h1 className="landing-title">Welcome to CIT-Care</h1>
                        <p className="landing-subtitle">Your centralized campus appointment and clinic management system.</p>
                        <div className="landing-actions">
                            <button onClick={() => navigate('/login')} className="btn-primary custom-btn">Sign In</button>
                            <button onClick={() => navigate('/register')} className="btn-secondary custom-btn">Create Account</button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Home;