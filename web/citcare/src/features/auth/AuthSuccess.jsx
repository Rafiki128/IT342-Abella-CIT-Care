import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthSuccess = () => {
    const navigate = useNavigate();

    useEffect(() => {
        // 1. Ask the backend "Who is currently logged in via OAuth2?"
        fetch('http://localhost:8080/api/v1/auth/me', { 
    method: 'GET',
    credentials: 'include' // THIS IS THE KEY: It sends the Spring session cookie
}) 
            .then(res => res.json())
            .then(response => {
                if (response && response.success) {
                    // 2. Save the real data (matching your manual login format)
                    localStorage.setItem('accessToken', response.data.accessToken);
                    localStorage.setItem('user', JSON.stringify(response.data.user));
                    
                    // 3. Go home!
                    navigate('/');
                } else {
                    console.error("Auth response failed", response);
                    navigate('/login');
                }
            })
            .catch(err => {
                console.error("Fetch error:", err);
                navigate('/login');
            });
    }, [navigate]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', flexDirection: 'column' }}>
            <h2>Verifying Google Account...</h2>
            <p>Please wait while we sync your CIT-Care profile.</p>
        </div>
    );
};

// CRITICAL: This line must be here!
export default AuthSuccess;