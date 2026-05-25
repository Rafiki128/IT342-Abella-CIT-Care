import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminDashboard.css';

const roleOptions = [
    { value: 'NEW', label: 'New' },
    { value: 'STUDENT', label: 'Student' },
    { value: 'MEDICAL_STAFF', label: 'Medical Staff' },
    { value: 'GUIDANCE_STAFF', label: 'Guidance Staff' },
    { value: 'ADMIN', label: 'Admin' }
];
const apiBaseUrl = 'http://localhost:8080';

const roleLabel = (role) => roleOptions.find((option) => option.value === role)?.label || role;

const parseResponse = async (response) => {
    const text = await response.text();
    if (!text) return {};

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [currentUser, setCurrentUser] = useState(null);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [savingUserId, setSavingUserId] = useState(null);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) {
            navigate('/login');
            return;
        }

        const parsedUser = JSON.parse(storedUser);
        setCurrentUser(parsedUser);

        if (parsedUser.role !== 'ADMIN') {
            setLoading(false);
            return;
        }

        fetchUsers();
    }, [navigate]);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${apiBaseUrl}/api/admin/users`, {
                headers: { 'X-User-Role': 'ADMIN' }
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Unable to load users.');
            }

            setUsers(Array.isArray(result.data) ? result.data : result);
        } catch (error) {
            setMessage({ text: error.message || 'Unable to load users. Make sure the backend is running.', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const filteredUsers = useMemo(() => {
        const query = searchTerm.trim().toLowerCase();
        if (!query) return users;

        return users.filter((user) => {
            return (
                user.fullName?.toLowerCase().includes(query) ||
                user.email?.toLowerCase().includes(query) ||
                user.role?.toLowerCase().includes(query)
            );
        });
    }, [users, searchTerm]);

    const roleCounts = useMemo(() => {
        return users.reduce((counts, user) => {
            counts[user.role] = (counts[user.role] || 0) + 1;
            return counts;
        }, {});
    }, [users]);

    const handleRoleChange = async (userId, role) => {
        setSavingUserId(userId);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/admin/update-role/${userId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'X-User-Role': 'ADMIN' },
                body: JSON.stringify({ role })
            });

            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Role update failed.');
            }

            const updatedRole = result.role || result.user?.role || role;

            setUsers((currentUsers) =>
                currentUsers.map((user) => (user.id === userId ? { ...user, role: updatedRole } : user))
            );

            if (currentUser?.id === userId) {
                const updatedCurrentUser = { ...currentUser, role: updatedRole };
                setCurrentUser(updatedCurrentUser);
                localStorage.setItem('user', JSON.stringify(updatedCurrentUser));
            }

            setMessage({ text: result.message, type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Role update failed.', type: 'error' });
        } finally {
            setSavingUserId(null);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    if (loading) {
        return (
            <div className="admin-page">
                <div className="admin-loading">Loading admin dashboard...</div>
            </div>
        );
    }

    if (!currentUser || currentUser.role !== 'ADMIN') {
        return (
            <div className="admin-page">
                <div className="admin-denied">
                    <h1>Admin Access Required</h1>
                    <p>Your account does not have permission to manage user roles.</p>
                    <button className="admin-primary-btn" onClick={() => navigate('/')}>Back to Home</button>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <nav className="admin-nav">
                <div>
                    <div className="admin-brand">CIT-Care Admin</div>
                    <span>Role Management</span>
                </div>
                <div className="admin-nav-actions">
                    <button className="admin-secondary-btn" onClick={() => navigate('/')}>Home</button>
                    <button className="admin-text-btn" onClick={handleLogout}>Logout</button>
                </div>
            </nav>

            <main className="admin-content">
                <section className="admin-header">
                    <div>
                        <p className="admin-kicker">Administrator Dashboard</p>
                        <h1>Manage User Roles</h1>
                    </div>
                    <button className="admin-primary-btn" onClick={fetchUsers}>Refresh Users</button>
                </section>

                {message.text && <div className={`admin-message ${message.type}`}>{message.text}</div>}

                <section className="admin-metrics" aria-label="User role totals">
                    {roleOptions.map((role) => (
                        <div className="admin-metric" key={role.value}>
                            <span>{role.label}</span>
                            <strong>{roleCounts[role.value] || 0}</strong>
                        </div>
                    ))}
                </section>

                <section className="admin-panel">
                    <div className="admin-panel-header">
                        <h2>Registered Users</h2>
                        <input
                            type="search"
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Search users"
                            aria-label="Search users"
                        />
                    </div>

                    <div className="admin-table-wrap">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Current Role</th>
                                    <th>Update Role</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredUsers.length > 0 ? filteredUsers.map((user) => (
                                    <tr key={user.id}>
                                        <td>{user.fullName}</td>
                                        <td>{user.email}</td>
                                        <td><span className={`admin-role ${user.role?.toLowerCase()}`}>{roleLabel(user.role)}</span></td>
                                        <td>
                                            <select
                                                value={user.role}
                                                disabled={savingUserId === user.id}
                                                onChange={(event) => handleRoleChange(user.id, event.target.value)}
                                                aria-label={`Update role for ${user.fullName}`}
                                            >
                                                {roleOptions.map((role) => (
                                                    <option value={role.value} key={role.value}>{role.label}</option>
                                                ))}
                                            </select>
                                        </td>
                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="4" className="admin-empty">No users found.</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default AdminDashboard;
