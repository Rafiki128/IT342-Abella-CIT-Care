import React from 'react';
import { useNavigate } from 'react-router-dom';
import './UniversalDashboard.css';

export const getStoredUser = () => {
    try {
        const userData = localStorage.getItem('user');
        return userData ? JSON.parse(userData) : null;
    } catch {
        localStorage.removeItem('user');
        return null;
    }
};

export const getDashboardPath = () => '/dashboard';

export const getDisplayName = (user) => {
    const name = user?.fullName || user?.name || [user?.firstName, user?.lastName].filter(Boolean).join(' ');
    if (name?.trim()) return name.trim();
    return user?.email?.split('@')[0] || 'CIT-Care User';
};

export const getInitials = (name = '') => {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return 'CC';
    return parts.slice(0, 2).map((part) => part[0]?.toUpperCase()).join('');
};

const roleBadge = (role) => {
    if (role === 'ADMIN') return { letter: 'A', label: 'Administrator' };
    if (role === 'MEDICAL_STAFF' || role === 'GUIDANCE_STAFF') return { letter: 'T', label: 'Staff' };
    if (role === 'STUDENT') return { letter: 'S', label: 'Student' };
    return { letter: 'N', label: 'New user' };
};


const LogoutIcon = () => (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
        <path d="M10 17l5-5-5-5" />
        <path d="M15 12H3" />
    </svg>
);

const MenuIcon = () => (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
        <path d="M4 7h16" />
        <path d="M4 12h16" />
        <path d="M4 17h16" />
    </svg>
);

const HomeIcon = () => (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
        <path d="M3 10.5 12 3l9 7.5" />
        <path d="M5 10v10h14V10" />
        <path d="M9 20v-6h6v6" />
    </svg>
);

const ProfileIcon = () => (
    <svg aria-hidden="true" viewBox="0 0 24 24" focusable="false">
        <path d="M20 21a8 8 0 0 0-16 0" />
        <path d="M12 13a5 5 0 1 0 0-10 5 5 0 0 0 0 10Z" />
    </svg>
);

const UniversalDashboard = ({
    user,
    title = 'CIT-Care',
    subtitle = 'Dashboard',
    children,
    pageClassName = '',
    contentClassName = '',
    showHomeLink = true,
    showProfileLink = true
}) => {
    const navigate = useNavigate();
    const displayName = getDisplayName(user);
    const badge = roleBadge(user?.role);

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    return (
        <div className={`universal-dashboard ${pageClassName}`}>
            <nav className="universal-dashboard-nav">
                <div className="universal-dashboard-nav-inner">
                <div className="universal-dashboard-brand-group">
                    <div className="universal-dashboard-logo">
                        <img src="/cit-logo.png" alt="" onError={(event) => { event.currentTarget.style.display = 'none'; }} />
                        <span>C</span>
                    </div>
                    <div>
                        <div className="universal-dashboard-brand">CIT-Care</div>
                    </div>
                </div>
                <div className="universal-dashboard-links" aria-hidden="true" />
                {user && (
                    <div className="universal-dashboard-user">
                        <span className="universal-dashboard-welcome">Welcome, {displayName}</span>
                        <div className="universal-dashboard-user-summary">
                            <button
                                className="universal-dashboard-role-icon"
                                title={badge.label}
                                aria-label={showProfileLink ? 'Open profile' : badge.label}
                                onClick={() => showProfileLink && navigate('/profile')}
                                type="button"
                            >
                                {badge.letter}
                            </button>
                        </div>
                        <details className="universal-dashboard-menu">
                            <summary aria-label="Open navigation menu">
                                <MenuIcon />
                            </summary>
                            <div className="universal-dashboard-menu-panel">
                                {showHomeLink && (
                                    <button onClick={() => navigate('/dashboard')}>
                                        <HomeIcon />
                                        Home
                                    </button>
                                )}
                                {showProfileLink && (
                                    <button onClick={() => navigate('/profile')}>
                                        <ProfileIcon />
                                        Profile
                                    </button>
                                )}
                                <button onClick={handleLogout} className="universal-dashboard-menu-danger">
                                    <LogoutIcon />
                                    Logout
                                </button>
                            </div>
                        </details>
                    </div>
                )}
                </div>
            </nav>

            <main className={contentClassName}>{children}</main>
        </div>
    );
};

export default UniversalDashboard;
