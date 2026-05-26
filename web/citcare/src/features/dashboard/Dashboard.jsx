import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import UniversalDashboard, { getDisplayName, getStoredUser } from '../../components/UniversalDashboard';
import BookingModal from '../appointments/BookingModal';
import './Dashboard.css';

const apiBaseUrl = 'http://localhost:8080';
const staffRoles = ['MEDICAL_STAFF', 'GUIDANCE_STAFF'];
const roleOptions = [
    { value: 'NEW', label: 'New' },
    { value: 'STUDENT', label: 'Student' },
    { value: 'MEDICAL_STAFF', label: 'Medical Staff' },
    { value: 'GUIDANCE_STAFF', label: 'Guidance Staff' },
    { value: 'ADMIN', label: 'Admin' }
];

const roleLabel = (role) => roleOptions.find((option) => option.value === role)?.label || role;
const appointmentStatusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'APPROVED', label: 'Confirmed' },
    { value: 'REJECTED', label: 'Cancelled' },
    { value: 'COMPLETED', label: 'Completed' }
];

const parseResponse = async (response) => {
    const text = await response.text();
    if (!text) return {};

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

const dashboardTitle = (role) => {
    if (role === 'ADMIN') return 'CIT-Care Admin';
    if (role === 'MEDICAL_STAFF') return 'CIT-Care Staff';
    if (role === 'GUIDANCE_STAFF') return 'CIT-Care Staff';
    return 'CIT-Care';
};

const dashboardSubtitle = (role) => {
    if (role === 'ADMIN') return 'Role Management';
    if (role === 'MEDICAL_STAFF') return 'Clinic appointments';
    if (role === 'GUIDANCE_STAFF') return 'Guidance appointments';
    return 'Student Dashboard';
};

const staffTitle = (role) => {
    if (role === 'MEDICAL_STAFF') return 'Medical Clinic Dashboard';
    if (role === 'GUIDANCE_STAFF') return 'Guidance Office Dashboard';
    return 'Staff Dashboard';
};

const getInitial = (name = '') => (name.trim()[0] || 'U').toUpperCase();
const roleInitial = (role = '') => {
    if (role === 'ADMIN') return 'A';
    if (role === 'STUDENT') return 'S';
    if (role === 'MEDICAL_STAFF' || role === 'GUIDANCE_STAFF') return 'T';
    return 'N';
};

const statusText = (status) => {
    if (status === 'APPROVED') return 'Confirmed';
    if (status === 'REJECTED') return 'Cancelled';
    if (status === 'COMPLETED') return 'Completed';
    return status || 'Pending';
};

const serviceType = (name = '') => (name.toLowerCase().includes('counsel') || name.toLowerCase().includes('guidance') ? 'Counseling' : 'Medical');
const appointmentStatusPriority = { PENDING: 0, APPROVED: 1, COMPLETED: 2, REJECTED: 3 };
const appointmentTimeValue = (appointment) => `${appointment.appointmentDate || '9999-12-31'} ${appointment.appointmentTime || '23:59'}`;

const sortAppointmentsByPriority = (items) => [...items].sort((first, second) => {
    const firstStatus = appointmentStatusPriority[first.status || 'PENDING'] ?? 9;
    const secondStatus = appointmentStatusPriority[second.status || 'PENDING'] ?? 9;

    if (firstStatus !== secondStatus) return firstStatus - secondStatus;
    return appointmentTimeValue(first).localeCompare(appointmentTimeValue(second));
});

const Dashboard = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [loadingUser, setLoadingUser] = useState(true);

    useEffect(() => {
        const storedUser = getStoredUser();
        if (!storedUser) {
            navigate('/login');
            return;
        }

        setUser(storedUser);
        setLoadingUser(false);
    }, [navigate]);

    if (loadingUser) {
        return (
            <div className="dashboard-page">
                <div className="dashboard-loading">Loading dashboard...</div>
            </div>
        );
    }

    const userRole = user?.role || 'STUDENT';

    return (
        <UniversalDashboard
            user={user}
            title={dashboardTitle(userRole)}
            subtitle={dashboardSubtitle(userRole)}
            pageClassName="dashboard-page"
            contentClassName="dashboard-content"
            showHomeLink={false}
            showDashboardLink={false}
        >
            {userRole === 'ADMIN' && <AdminPanel currentUser={user} setCurrentUser={setUser} />}
            {staffRoles.includes(userRole) && <StaffPanel currentUser={user} />}
            {!['ADMIN', ...staffRoles].includes(userRole) && <StudentPanel user={user} />}
        </UniversalDashboard>
    );
};

const AdminPanel = ({ currentUser, setCurrentUser }) => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [savingUserId, setSavingUserId] = useState(null);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [roleMenuOpen, setRoleMenuOpen] = useState(false);
    const [actionMenuUserId, setActionMenuUserId] = useState(null);
    const [roleModal, setRoleModal] = useState({ isOpen: false, user: null, role: '' });
    const [nameModal, setNameModal] = useState({ isOpen: false, user: null, fullName: '' });

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

    useEffect(() => {
        fetchUsers();
    }, []);

    const filteredUsers = useMemo(() => {
        const query = searchTerm.trim().toLowerCase();

        return users.filter((item) => (
            (!roleFilter || item.role === roleFilter) &&
            (
                !query ||
                item.fullName?.toLowerCase().includes(query) ||
                item.email?.toLowerCase().includes(query) ||
                item.role?.toLowerCase().includes(query)
            )
        ));
    }, [users, searchTerm, roleFilter]);

    const roleCounts = useMemo(() => {
        return users.reduce((counts, item) => {
            counts[item.role] = (counts[item.role] || 0) + 1;
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
            setUsers((items) => items.map((item) => (item.id === userId ? { ...item, role: updatedRole } : item)));

            if (currentUser?.id === userId) {
                const updatedCurrentUser = { ...currentUser, role: updatedRole };
                setCurrentUser(updatedCurrentUser);
                localStorage.setItem('user', JSON.stringify(updatedCurrentUser));
            }

            setMessage({ text: result.message, type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Role update failed.', type: 'error' });
            return false;
        } finally {
            setSavingUserId(null);
        }

        return true;
    };

    const handleNameChange = async (userId, fullName) => {
        const nextName = fullName.trim();
        if (!nextName) {
            setMessage({ text: 'Full name is required.', type: 'error' });
            return false;
        }

        setSavingUserId(userId);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/admin/users/${userId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'X-User-Role': 'ADMIN' },
                body: JSON.stringify({ fullName: nextName })
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Name update failed.');
            }

            const updatedUser = result.user || {};
            const updatedName = updatedUser.fullName || nextName;
            setUsers((items) => items.map((item) => (item.id === userId ? { ...item, fullName: updatedName } : item)));

            if (currentUser?.id === userId) {
                const updatedCurrentUser = { ...currentUser, fullName: updatedName };
                setCurrentUser(updatedCurrentUser);
                localStorage.setItem('user', JSON.stringify(updatedCurrentUser));
            }

            setMessage({ text: result.message || 'User name updated.', type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Name update failed.', type: 'error' });
            return false;
        } finally {
            setSavingUserId(null);
        }

        return true;
    };

    const handleDeleteUser = async (item) => {
        setActionMenuUserId(null);
        if (currentUser?.id === item.id) {
            setMessage({ text: 'You cannot delete your own admin account.', type: 'error' });
            return;
        }

        const confirmed = window.confirm(`Delete ${item.fullName}? This action cannot be undone.`);
        if (!confirmed) return;

        setSavingUserId(item.id);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/admin/users/${item.id}`, {
                method: 'DELETE',
                headers: { 'X-User-Role': 'ADMIN', 'X-User-Id': String(currentUser?.id || '') }
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Delete failed.');
            }

            setUsers((items) => items.filter((userItem) => userItem.id !== item.id));
            setMessage({ text: result.message || 'User deleted.', type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Delete failed.', type: 'error' });
        } finally {
            setSavingUserId(null);
        }
    };

    const openRoleModal = (item) => {
        setActionMenuUserId(null);
        setRoleModal({ isOpen: true, user: item, role: item.role || 'NEW' });
    };

    const openNameModal = (item) => {
        setActionMenuUserId(null);
        setNameModal({ isOpen: true, user: item, fullName: item.fullName || '' });
    };

    if (loading) {
        return <div className="dashboard-loading">Loading admin dashboard...</div>;
    }

    return (
        <>
            {message.text && <div className={`dashboard-message ${message.type}`}>{message.text}</div>}

            <section className="admin-stat-grid" aria-label="User totals">
                <div className="admin-stat-card">
                    <div>
                        <span>Total Users</span>
                        <strong>{users.length}</strong>
                    </div>
                    <div className="admin-stat-icon admin" aria-hidden="true">A</div>
                </div>
                <div className="admin-stat-card">
                    <div>
                        <span>Staff Members</span>
                        <strong>{(roleCounts.MEDICAL_STAFF || 0) + (roleCounts.GUIDANCE_STAFF || 0)}</strong>
                    </div>
                    <div className="admin-stat-icon staff" aria-hidden="true">T</div>
                </div>
                <div className="admin-stat-card">
                    <div>
                        <span>Students</span>
                        <strong>{roleCounts.STUDENT || 0}</strong>
                    </div>
                    <div className="admin-stat-icon student" aria-hidden="true">S</div>
                </div>
            </section>

            <section className="management-panel">
                <div className="management-panel-title">
                    <h2>User Management</h2>
                    <div className="management-tools">
                        <input
                            type="search"
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Search users..."
                            aria-label="Search users"
                        />
                        <div className={`dashboard-filter-control role-filter ${roleFilter ? roleFilter.toLowerCase() : 'all'}`}>
                            <button
                                className="status-filter-button"
                                type="button"
                                onClick={() => setRoleMenuOpen((open) => !open)}
                                aria-expanded={roleMenuOpen}
                                aria-haspopup="listbox"
                                aria-label="Filter users by role"
                            >
                                <span>Role</span>
                                <strong>{roleFilter ? roleLabel(roleFilter) : 'All Roles'}</strong>
                            </button>
                            {roleMenuOpen && (
                                <div className="status-filter-menu role-filter-menu" role="listbox" aria-label="User role options">
                                    <button
                                        className={`status-filter-option role-filter-option all${roleFilter === '' ? ' selected' : ''}`}
                                        type="button"
                                        role="option"
                                        aria-selected={roleFilter === ''}
                                        onClick={() => {
                                            setRoleFilter('');
                                            setRoleMenuOpen(false);
                                            setActionMenuUserId(null);
                                        }}
                                    >
                                        <span aria-hidden="true" />
                                        All Roles
                                    </button>
                                    {roleOptions.map((role) => (
                                        <button
                                            className={`status-filter-option role-filter-option ${role.value.toLowerCase()}${roleFilter === role.value ? ' selected' : ''}`}
                                            type="button"
                                            key={role.value}
                                            role="option"
                                            aria-selected={roleFilter === role.value}
                                            onClick={() => {
                                                setRoleFilter(role.value);
                                                setRoleMenuOpen(false);
                                                setActionMenuUserId(null);
                                            }}
                                        >
                                            <span aria-hidden="true" />
                                            {role.label}
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="dashboard-table-wrap">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th className="admin-user-header">User</th>
                                <th>Role</th>
                                <th>Department</th>
                                <th className="admin-actions-header">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredUsers.length > 0 ? filteredUsers.map((item) => (
                                <tr key={item.id}>
                                    <td className="admin-user-table-cell">
                                        <div className="management-user-cell">
                                            <span className={`role-avatar ${item.role?.toLowerCase()}`}>{roleInitial(item.role)}</span>
                                            <div>
                                                <strong>{item.fullName}</strong>
                                                <small>{item.email}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span className={`role-badge ${item.role?.toLowerCase()}`}>{roleLabel(item.role)}</span>
                                    </td>
                                    <td>{item.role === 'MEDICAL_STAFF' ? 'Medical' : item.role === 'GUIDANCE_STAFF' ? 'Counseling' : '-'}</td>
                                    <td className="admin-actions-table-cell">
                                        <div className="admin-action-menu">
                                            <button
                                                className="admin-action-toggle"
                                                type="button"
                                                disabled={savingUserId === item.id}
                                                onClick={() => setActionMenuUserId((openId) => (openId === item.id ? null : item.id))}
                                                aria-expanded={actionMenuUserId === item.id}
                                                aria-label={`Open actions for ${item.fullName}`}
                                            >
                                                <span className="admin-action-dot" aria-hidden="true" />
                                                <span className="admin-action-dot" aria-hidden="true" />
                                                <span className="admin-action-dot" aria-hidden="true" />
                                            </button>
                                            {actionMenuUserId === item.id && (
                                                <div className="admin-action-dropdown">
                                                    <button type="button" onClick={() => openRoleModal(item)}>Edit Role</button>
                                                    <button type="button" onClick={() => openNameModal(item)}>Edit Name</button>
                                                    <button
                                                        className="danger"
                                                        type="button"
                                                        disabled={currentUser?.id === item.id}
                                                        onClick={() => handleDeleteUser(item)}
                                                    >
                                                        Delete User
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="4" className="dashboard-empty">No users found.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {roleModal.isOpen && (
                <RoleEditModal
                    modal={roleModal}
                    saving={savingUserId === roleModal.user?.id}
                    onChange={(role) => setRoleModal((modal) => ({ ...modal, role }))}
                    onCancel={() => setRoleModal({ isOpen: false, user: null, role: '' })}
                    onSubmit={async (event) => {
                        event.preventDefault();
                        const updated = await handleRoleChange(roleModal.user.id, roleModal.role);
                        if (updated) setRoleModal({ isOpen: false, user: null, role: '' });
                    }}
                />
            )}

            {nameModal.isOpen && (
                <NameEditModal
                    modal={nameModal}
                    saving={savingUserId === nameModal.user?.id}
                    onChange={(fullName) => setNameModal((modal) => ({ ...modal, fullName }))}
                    onCancel={() => setNameModal({ isOpen: false, user: null, fullName: '' })}
                    onSubmit={async (event) => {
                        event.preventDefault();
                        const updated = await handleNameChange(nameModal.user.id, nameModal.fullName);
                        if (updated) setNameModal({ isOpen: false, user: null, fullName: '' });
                    }}
                />
            )}
        </>
    );
};

const RoleEditModal = ({ modal, saving, onChange, onCancel, onSubmit }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="role-modal-title">
            <form onSubmit={onSubmit}>
                <div className="dashboard-modal-header">
                    <div>
                        <p className="dashboard-kicker">Edit Role</p>
                        <h2 id="role-modal-title">{modal.user?.fullName || 'Selected user'}</h2>
                    </div>
                    <button type="button" className="dashboard-modal-close" onClick={onCancel} aria-label="Close edit role modal">x</button>
                </div>
                <label className="dashboard-modal-label" htmlFor="editRole">Role</label>
                <select
                    id="editRole"
                    className="dashboard-modal-input"
                    value={modal.role}
                    onChange={(event) => onChange(event.target.value)}
                    autoFocus
                >
                    {roleOptions.map((role) => (
                        <option value={role.value} key={role.value}>{role.label}</option>
                    ))}
                </select>
                <div className="dashboard-modal-actions">
                    <button type="button" className="dashboard-secondary-action" onClick={onCancel}>Cancel</button>
                    <button type="submit" className="dashboard-primary-action" disabled={saving}>Save Role</button>
                </div>
            </form>
        </section>
    </div>
);

const NameEditModal = ({ modal, saving, onChange, onCancel, onSubmit }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="name-modal-title">
            <form onSubmit={onSubmit}>
                <div className="dashboard-modal-header">
                    <div>
                        <p className="dashboard-kicker">Edit Name</p>
                        <h2 id="name-modal-title">{modal.user?.email || 'Selected user'}</h2>
                    </div>
                    <button type="button" className="dashboard-modal-close" onClick={onCancel} aria-label="Close edit name modal">x</button>
                </div>
                <label className="dashboard-modal-label" htmlFor="editFullName">Full Name</label>
                <input
                    id="editFullName"
                    className="dashboard-modal-input"
                    type="text"
                    value={modal.fullName}
                    onChange={(event) => onChange(event.target.value)}
                    autoFocus
                    required
                />
                <div className="dashboard-modal-actions">
                    <button type="button" className="dashboard-secondary-action" onClick={onCancel}>Cancel</button>
                    <button type="submit" className="dashboard-primary-action" disabled={saving}>Save Name</button>
                </div>
            </form>
        </section>
    </div>
);

const StaffPanel = ({ currentUser }) => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [savingId, setSavingId] = useState(null);
    const [message, setMessage] = useState({ text: '', type: '' });
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [statusMenuOpen, setStatusMenuOpen] = useState(false);
    const [rejectModal, setRejectModal] = useState({ isOpen: false, appointment: null, action: 'reject' });
    const [rejectReason, setRejectReason] = useState('');
    const [rejectError, setRejectError] = useState('');
    const [reasonModal, setReasonModal] = useState({ isOpen: false, appointment: null });
    const [actionMenuAppointmentId, setActionMenuAppointmentId] = useState(null);

    const fetchAppointments = async (staffId = currentUser?.id) => {
        if (!staffId) return;

        setLoading(true);
        try {
            const response = await fetch(`${apiBaseUrl}/api/appointments/staff/${staffId}`);
            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.message || 'Unable to load appointments.');
            }

            setAppointments(result);
        } catch (error) {
            setMessage({ text: error.message || 'Unable to load appointments.', type: 'error' });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppointments(currentUser.id);
    }, [currentUser.id]);

    const filteredAppointments = useMemo(() => {
        const query = searchTerm.trim().toLowerCase();

        return appointments.filter((appointment) => (
            (!statusFilter || appointment.status === statusFilter) &&
            (
                !query ||
                appointment.student?.fullName?.toLowerCase().includes(query) ||
                appointment.student?.email?.toLowerCase().includes(query) ||
                String(appointment.student?.id || appointment.id).includes(query) ||
                appointment.service?.name?.toLowerCase().includes(query) ||
                appointment.status?.toLowerCase().includes(query)
            )
        ));
    }, [appointments, searchTerm, statusFilter]);

    const counts = useMemo(() => {
        return appointments.reduce((totals, appointment) => {
            totals[appointment.status] = (totals[appointment.status] || 0) + 1;
            return totals;
        }, {});
    }, [appointments]);

    const updateAppointment = async (appointmentId, action, reason = '') => {
        setSavingId(appointmentId);
        setActionMenuAppointmentId(null);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/appointments/${appointmentId}/${action}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ staffId: currentUser.id, reason })
            });
            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.message || `Unable to ${action} appointment.`);
            }

            setAppointments((items) => items.map((item) => (item.id === appointmentId ? result : item)));
            const actionLabel = action === 'approve' ? 'approved' : action === 'complete' ? 'completed' : action === 'cancel' ? 'cancelled' : 'rejected';
            setMessage({ text: `Appointment ${actionLabel}.`, type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || `Unable to ${action} appointment.`, type: 'error' });
            return false;
        } finally {
            setSavingId(null);
        }

        return true;
    };

    const openRejectModal = (appointment) => {
        setActionMenuAppointmentId(null);
        setRejectModal({ isOpen: true, appointment, action: 'reject' });
        setRejectReason('');
        setRejectError('');
    };

    const openCancelModal = (appointment) => {
        setActionMenuAppointmentId(null);
        setRejectModal({ isOpen: true, appointment, action: 'cancel' });
        setRejectReason('');
        setRejectError('');
    };

    const closeRejectModal = () => {
        if (savingId) return;
        setRejectModal({ isOpen: false, appointment: null, action: 'reject' });
        setRejectReason('');
        setRejectError('');
    };

    const handleRejectSubmit = async (event) => {
        event.preventDefault();
        const reason = rejectReason.trim();
        if (!reason) {
            setRejectError('Please provide a reason before rejecting this appointment.');
            return;
        }

        const updated = await updateAppointment(rejectModal.appointment.id, rejectModal.action || 'reject', reason);
        if (updated) {
            setRejectModal({ isOpen: false, appointment: null, action: 'reject' });
            setRejectReason('');
            setRejectError('');
        }
    };

    if (loading) {
        return <div className="dashboard-loading">Loading staff dashboard...</div>;
    }

    return (
        <>
            <section className="staff-heading">
                <div>
                    <h1>Appointment Management</h1>
                    <p>Review and manage student appointments</p>
                </div>
                <div className="management-tools">
                    <input
                        type="search"
                        value={searchTerm}
                        onChange={(event) => setSearchTerm(event.target.value)}
                        placeholder="Search student or ID..."
                        aria-label="Search appointments"
                    />
                    <div className={`dashboard-filter-control status-filter ${statusFilter ? statusFilter.toLowerCase() : 'all'}`}>
                        <button
                            className="status-filter-button"
                            type="button"
                            onClick={() => setStatusMenuOpen((open) => !open)}
                            aria-expanded={statusMenuOpen}
                            aria-haspopup="listbox"
                            aria-label="Filter appointment status"
                        >
                            <span>Status</span>
                            <strong>{appointmentStatusOptions.find((option) => option.value === statusFilter)?.label || 'All Statuses'}</strong>
                        </button>
                        {statusMenuOpen && (
                            <div className="status-filter-menu" role="listbox" aria-label="Appointment status options">
                                {appointmentStatusOptions.map((option) => (
                                    <button
                                        className={`status-filter-option ${option.value ? option.value.toLowerCase() : 'all'}${statusFilter === option.value ? ' selected' : ''}`}
                                        type="button"
                                        key={option.value || 'all'}
                                        role="option"
                                        aria-selected={statusFilter === option.value}
                                        onClick={() => {
                                            setStatusFilter(option.value);
                                            setStatusMenuOpen(false);
                                            setActionMenuAppointmentId(null);
                                        }}
                                    >
                                        <span aria-hidden="true" />
                                        {option.label}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </section>

            {message.text && <div className={`dashboard-message ${message.type}`}>{message.text}</div>}

            <section className="management-panel staff-panel-modern">
                <div className="dashboard-table-wrap">
                    <table className="dashboard-table">
                        <thead>
                            <tr>
                                <th>Student</th>
                                <th>Appointment Details</th>
                                <th>Date & Time</th>
                                <th>Status</th>
                                <th className="staff-actions-header">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredAppointments.length > 0 ? filteredAppointments.map((appointment) => (
                                <tr key={appointment.id}>
                                    <td className="staff-student-table-cell">
                                        <div className="management-user-cell">
                                            <span className="role-avatar student">S</span>
                                            <div>
                                                <strong>{appointment.student?.fullName || 'Unknown student'}</strong>
                                                <small>ID: {appointment.student?.id || appointment.id}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <strong>{appointment.service?.name || 'Service unavailable'}</strong>
                                        <span className={`service-dot ${serviceType(appointment.service?.name).toLowerCase()}`}>{serviceType(appointment.service?.name)}</span>
                                    </td>
                                    <td>
                                        <span>{appointment.appointmentDate || 'TBA'}</span>
                                        <span>{appointment.appointmentTime || 'TBA'}</span>
                                    </td>
                                    <td><span className={`dashboard-status ${appointment.status?.toLowerCase()}`}>{statusText(appointment.status)}</span></td>
                                    <td className="staff-actions-table-cell">
                                        <div className="admin-action-menu">
                                            <button
                                                className="admin-action-toggle"
                                                type="button"
                                                disabled={savingId === appointment.id}
                                                onClick={() => setActionMenuAppointmentId((openId) => (openId === appointment.id ? null : appointment.id))}
                                                aria-expanded={actionMenuAppointmentId === appointment.id}
                                                aria-label={`Open actions for ${appointment.student?.fullName || 'selected appointment'}`}
                                            >
                                                <span className="admin-action-dot" aria-hidden="true" />
                                                <span className="admin-action-dot" aria-hidden="true" />
                                                <span className="admin-action-dot" aria-hidden="true" />
                                            </button>
                                            {actionMenuAppointmentId === appointment.id && (
                                                <div className="admin-action-dropdown">
                                                    {appointment.status === 'PENDING' && (
                                                        <>
                                                            <button type="button" onClick={() => updateAppointment(appointment.id, 'approve')}>Approve</button>
                                                            <button className="danger" type="button" onClick={() => openRejectModal(appointment)}>Reject</button>
                                                        </>
                                                    )}
                                                    {appointment.status === 'APPROVED' && (
                                                        <>
                                                            <button type="button" onClick={() => updateAppointment(appointment.id, 'complete')}>Mark Complete</button>
                                                            <button className="danger" type="button" onClick={() => openCancelModal(appointment)}>Cancel Appointment</button>
                                                        </>
                                                    )}
                                                    {appointment.status === 'REJECTED' && appointment.rejectionReason && (
                                                        <button
                                                            type="button"
                                                            onClick={() => {
                                                                setActionMenuAppointmentId(null);
                                                                setReasonModal({ isOpen: true, appointment });
                                                            }}
                                                        >
                                                            View Reason
                                                        </button>
                                                    )}
                                                    {appointment.status === 'REJECTED' && !appointment.rejectionReason && (
                                                        <button type="button" disabled>No actions available</button>
                                                    )}
                                                    {appointment.status === 'COMPLETED' && (
                                                        <button type="button" disabled>No actions available</button>
                                                    )}
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr>
                                    <td colSpan="5" className="dashboard-empty">No appointments found.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {rejectModal.isOpen && (
                <ReasonFormModal
                    appointment={rejectModal.appointment}
                    action={rejectModal.action}
                    reason={rejectReason}
                    error={rejectError}
                    saving={savingId === rejectModal.appointment?.id}
                    onChange={(value) => {
                        setRejectReason(value);
                        setRejectError('');
                    }}
                    onCancel={closeRejectModal}
                    onSubmit={handleRejectSubmit}
                />
            )}

            {reasonModal.isOpen && (
                <ReadOnlyReasonModal
                    appointment={reasonModal.appointment}
                    onClose={() => setReasonModal({ isOpen: false, appointment: null })}
                />
            )}
        </>
    );
};

const StudentPanel = ({ user }) => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isAllAppointmentsOpen, setIsAllAppointmentsOpen] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [appointments, setAppointments] = useState([]);
    const [services, setServices] = useState([]);
    const [formData, setFormData] = useState({ serviceId: '', appointmentDate: '', appointmentTime: '', reason: '', office: '' });

    const loadUserAppointments = async (userId) => {
        try {
            const res = await fetch(`${apiBaseUrl}/api/appointments/student/${userId}`);
            if (res.ok) return await res.json();
        } catch (err) {
            console.error(err);
        }
        return [];
    };

    useEffect(() => {
        loadUserAppointments(user.id).then(setAppointments);

        fetch(`${apiBaseUrl}/api/services`)
            .then((res) => res.json())
            .then((data) => setServices(Array.isArray(data) ? data : data?.data || []))
            .catch(() => console.log('Backend offline'));
    }, [user.id]);

    const appointmentCounts = appointments.reduce((counts, appointment) => {
        const status = appointment.status || 'PENDING';
        counts[status] = (counts[status] || 0) + 1;
        return counts;
    }, {});

    const userRole = user?.role || 'STUDENT';
    const canBookAppointments = userRole === 'STUDENT';
    const displayName = getDisplayName(user);
    const prioritizedAppointments = useMemo(() => sortAppointmentsByPriority(appointments), [appointments]);

    const handleInputChange = (event) => setFormData({ ...formData, [event.target.name]: event.target.value });

    const openBookingModal = (type) => {
        const isGuidance = type === 'guidance';
        const selectedService = services.find((service) => {
            const name = service.name?.toLowerCase() || '';
            return isGuidance ? name.includes('guidance') || name.includes('counsel') : name.includes('medical') || name.includes('clinic');
        });

        setFormData({
            serviceId: selectedService?.id ? String(selectedService.id) : '',
            appointmentDate: '',
            appointmentTime: '',
            reason: '',
            office: isGuidance ? 'Guidance Office' : 'Medical Clinic'
        });
        setIsModalOpen(true);
    };

    const handleBooking = async (event) => {
        event.preventDefault();
        const res = await fetch(`${apiBaseUrl}/api/appointments/book`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...formData, studentId: user.id })
        });
        if (res.ok) {
            alert('Booking Successful!');
            setIsModalOpen(false);
            setAppointments(await loadUserAppointments(user.id));
        }
    };

    return (
        <>
            <section className="student-hero">
                <div>
                    <h1>Hello, {displayName}!</h1>
                    <p>How are you feeling today? We're here to support your health and wellness journey. Schedule a checkup or counseling session whenever you need.</p>
                </div>
            </section>

            {!canBookAppointments && (
                <div className="dashboard-notice">
                    Your account role is <strong>{userRole}</strong>. Appointment booking is available once your account is assigned the Student role.
                </div>
            )}

            <div className="student-layout">
                <div className="student-main">
                    <section className="student-card">
                        <div className="section-title-row">
                            <h2>Upcoming Appointments</h2>
                            <button type="button" onClick={() => setIsAllAppointmentsOpen(true)} disabled={appointments.length === 0}>View All</button>
                        </div>
                        <div className="appointment-list">
                            {prioritizedAppointments.length > 0 ? prioritizedAppointments.slice(0, 3).map((appointment) => (
                                <button
                                    className="appointment-card"
                                    key={appointment.id}
                                    type="button"
                                    onClick={() => setSelectedAppointment(appointment)}
                                >
                                    <div className={`appointment-icon ${serviceType(appointment.service?.name) === 'Medical' ? 'medical-icon' : 'guidance-icon'}`} aria-hidden="true" />
                                    <div className="appointment-info">
                                        <h3>{appointment.service?.name || 'Service unavailable'}</h3>
                                        <p>{appointment.appointmentTime || 'TBA'} <span>{appointment.appointmentDate || 'TBA'}</span></p>
                                        <p>{appointment.office || 'CIT-Care Office'}</p>
                                    </div>
                                    <span className={`dashboard-status ${(appointment.status || 'pending').toLowerCase()}`}>{statusText(appointment.status || 'PENDING')}</span>
                                </button>
                            )) : (
                                <div className="student-empty">No appointments found.</div>
                            )}
                        </div>
                    </section>

                    <section className="student-booking">
                        <h2>Book New Appointment</h2>
                        <div className="booking-service-grid">
                            <article>
                                <div className="booking-icon medical-icon" aria-hidden="true" />
                                <h3>Medical Clinic</h3>
                                <p>General checkups, consultations, and referrals.</p>
                                <button disabled={!canBookAppointments} onClick={() => openBookingModal('medical')}>Book Appointment</button>
                            </article>
                            <article>
                                <div className="booking-icon guidance-icon" aria-hidden="true" />
                                <h3>Guidance Counseling</h3>
                                <p>Mental health, academic and career guidance.</p>
                                <button disabled={!canBookAppointments} onClick={() => openBookingModal('guidance')}>Book Appointment</button>
                            </article>
                        </div>
                    </section>
                </div>

                <aside className="student-sidebar">
                    <section className="student-card">
                        <h2>Quick Actions</h2>
                        <div className="quick-grid">
                            {['Emergency Contact', 'Talk to Counselor', 'Medical Records', 'FAQs & Help'].map((label, index) => (
                                <button key={label} type="button">
                                    <span className={`quick-icon quick-${index}`}>{index + 1}</span>
                                    <strong>{label}</strong>
                                    <small>{index === 0 ? '24/7 Support' : index === 1 ? 'Immediate Support' : index === 2 ? 'View History' : 'Get Answers'}</small>
                                </button>
                            ))}
                        </div>
                    </section>
                    <section className="talk-card">
                        <div className="talk-bubble guidance-icon" aria-hidden="true" />
                        <h3>Need to Talk?</h3>
                        <p>Our counselors are available for walk-ins or urgent calls.</p>
                        <button type="button">Connect Now</button>
                    </section>
                    <section className="tip-card">
                        <h3>Daily Tip</h3>
                        <p>"Taking short breaks during study sessions improves focus and retention. Try the 20-20-20 rule!"</p>
                    </section>
                </aside>
            </div>

            <footer className="dashboard-footer">
                <div>
                    <h3>CIT-Care</h3>
                    <p>Your trusted partner for healthcare and wellness services.</p>
                </div>
                <div>
                    <h3>Services</h3>
                    <p>Medical Clinic</p>
                    <p>Counseling</p>
                    <p>Wellness Programs</p>
                </div>
                <div>
                    <h3>Quick Links</h3>
                    <p>About Us</p>
                    <p>Contact</p>
                    <p>Privacy Policy</p>
                </div>
                <div>
                    <h3>Contact</h3>
                    <p>Emergency: (555) 911-HELP</p>
                    <p>Email: care@cit.edu</p>
                </div>
            </footer>

            <BookingModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                services={services}
                formData={formData}
                handleInputChange={handleInputChange}
                handleBooking={handleBooking}
            />

            {selectedAppointment && (
                <AppointmentDetailsModal
                    appointment={selectedAppointment}
                    onClose={() => setSelectedAppointment(null)}
                />
            )}

            {isAllAppointmentsOpen && (
                <AllAppointmentsModal
                    appointments={prioritizedAppointments}
                    onClose={() => setIsAllAppointmentsOpen(false)}
                    onSelect={(appointment) => {
                        setIsAllAppointmentsOpen(false);
                        setSelectedAppointment(appointment);
                    }}
                />
            )}
        </>
    );
};

const AppointmentDetailsModal = ({ appointment, onClose }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal appointment-details-modal" role="dialog" aria-modal="true" aria-labelledby="appointment-details-title">
            <div className="dashboard-modal-header">
                <div>
                    <p className="dashboard-kicker">Appointment Details</p>
                    <h2 id="appointment-details-title">{appointment.service?.name || 'Service unavailable'}</h2>
                </div>
                <button type="button" className="dashboard-modal-close" onClick={onClose} aria-label="Close appointment details">x</button>
            </div>
            <div className="appointment-details-grid">
                <AppointmentDetail label="Status" value={statusText(appointment.status || 'PENDING')} badgeClass={`dashboard-status ${(appointment.status || 'pending').toLowerCase()}`} />
                <AppointmentDetail label="Service Type" value={serviceType(appointment.service?.name)} />
                <AppointmentDetail label="Date" value={appointment.appointmentDate || 'TBA'} />
                <AppointmentDetail label="Time" value={appointment.appointmentTime || 'TBA'} />
                <AppointmentDetail label="Room / Location" value={appointment.office || 'CIT-Care Office'} />
                <AppointmentDetail label="Reason / Notes" value={appointment.reason || 'No notes provided.'} wide />
                {appointment.rejectionReason && (
                    <AppointmentDetail label="Cancellation Reason" value={appointment.rejectionReason} wide />
                )}
            </div>
        </section>
    </div>
);

const AllAppointmentsModal = ({ appointments, onClose, onSelect }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal all-appointments-modal" role="dialog" aria-modal="true" aria-labelledby="all-appointments-title">
            <div className="dashboard-modal-header">
                <div>
                    <p className="dashboard-kicker">All Appointments</p>
                    <h2 id="all-appointments-title">Appointment History</h2>
                </div>
                <button type="button" className="dashboard-modal-close" onClick={onClose} aria-label="Close all appointments">x</button>
            </div>
            <div className="all-appointments-list">
                {appointments.length > 0 ? appointments.map((appointment) => (
                    <button
                        className="appointment-card appointment-card-compact"
                        key={appointment.id}
                        type="button"
                        onClick={() => onSelect(appointment)}
                    >
                        <div className={`appointment-icon ${serviceType(appointment.service?.name) === 'Medical' ? 'medical-icon' : 'guidance-icon'}`} aria-hidden="true" />
                        <div className="appointment-info">
                            <h3>{appointment.service?.name || 'Service unavailable'}</h3>
                            <p>{appointment.appointmentTime || 'TBA'} <span>{appointment.appointmentDate || 'TBA'}</span></p>
                            <p>{appointment.office || 'CIT-Care Office'}</p>
                        </div>
                        <span className={`dashboard-status ${(appointment.status || 'pending').toLowerCase()}`}>{statusText(appointment.status || 'PENDING')}</span>
                    </button>
                )) : (
                    <div className="student-empty">No appointments found.</div>
                )}
            </div>
        </section>
    </div>
);

const AppointmentDetail = ({ label, value, badgeClass = '', wide = false }) => (
    <div className={`appointment-detail-item${wide ? ' wide' : ''}`}>
        <span>{label}</span>
        {badgeClass ? <strong className={badgeClass}>{value}</strong> : <strong>{value}</strong>}
    </div>
);

const ReasonFormModal = ({ appointment, action = 'reject', reason, error, saving, onChange, onCancel, onSubmit }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="reject-modal-title">
            <form onSubmit={onSubmit}>
                <div className="dashboard-modal-header">
                    <div>
                        <p className="dashboard-kicker">{action === 'cancel' ? 'Cancel Appointment' : 'Reject Appointment'}</p>
                        <h2 id="reject-modal-title">{appointment?.student?.fullName || 'Selected student'}</h2>
                    </div>
                    <button type="button" className="dashboard-modal-close" onClick={onCancel} aria-label="Close reject reason modal">x</button>
                </div>
                <label className="dashboard-modal-label" htmlFor="rejectReason">Reason</label>
                <textarea
                    id="rejectReason"
                    value={reason}
                    onChange={(event) => onChange(event.target.value)}
                    placeholder={action === 'cancel' ? 'Explain why this confirmed appointment is being cancelled.' : 'Explain why this appointment is being rejected.'}
                    rows="5"
                    autoFocus
                />
                {error && <div className="dashboard-modal-error">{error}</div>}
                <div className="dashboard-modal-actions">
                    <button type="button" className="dashboard-secondary-action" onClick={onCancel}>Cancel</button>
                    <button type="submit" className="dashboard-reject-btn" disabled={saving}>{action === 'cancel' ? 'Confirm Cancel' : 'Confirm Reject'}</button>
                </div>
            </form>
        </section>
    </div>
);

const ReadOnlyReasonModal = ({ appointment, onClose }) => (
    <div className="dashboard-modal-backdrop" role="presentation">
        <section className="dashboard-modal" role="dialog" aria-modal="true" aria-labelledby="reason-modal-title">
            <div className="dashboard-modal-header">
                <div>
                    <p className="dashboard-kicker">Rejection Reason</p>
                    <h2 id="reason-modal-title">{appointment?.student?.fullName || 'Selected student'}</h2>
                </div>
                <button type="button" className="dashboard-modal-close" onClick={onClose} aria-label="Close rejection reason modal">x</button>
            </div>
            <p className="dashboard-reason-text">{appointment?.rejectionReason}</p>
        </section>
    </div>
);

export default Dashboard;
