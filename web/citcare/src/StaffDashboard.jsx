import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './StaffDashboard.css';

const apiBaseUrl = 'http://localhost:8080';
const staffRoles = ['MEDICAL_STAFF', 'GUIDANCE_STAFF'];

const dashboardTitle = (role) => {
    if (role === 'MEDICAL_STAFF') return 'Medical Clinic Dashboard';
    if (role === 'GUIDANCE_STAFF') return 'Guidance Office Dashboard';
    return 'Staff Dashboard';
};

const serviceLabel = (role) => {
    if (role === 'MEDICAL_STAFF') return 'Clinic appointments';
    if (role === 'GUIDANCE_STAFF') return 'Guidance appointments';
    return 'Assigned appointments';
};

const StaffDashboard = () => {
    const navigate = useNavigate();
    const [currentUser, setCurrentUser] = useState(null);
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [savingId, setSavingId] = useState(null);
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

        if (!staffRoles.includes(parsedUser.role)) {
            setLoading(false);
            return;
        }

        fetchAppointments(parsedUser.id);
    }, [navigate]);

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

    const filteredAppointments = useMemo(() => {
        const query = searchTerm.trim().toLowerCase();
        if (!query) return appointments;

        return appointments.filter((appointment) => (
            appointment.student?.fullName?.toLowerCase().includes(query) ||
            appointment.student?.email?.toLowerCase().includes(query) ||
            appointment.service?.name?.toLowerCase().includes(query) ||
            appointment.status?.toLowerCase().includes(query)
        ));
    }, [appointments, searchTerm]);

    const counts = useMemo(() => {
        return appointments.reduce((totals, appointment) => {
            totals[appointment.status] = (totals[appointment.status] || 0) + 1;
            return totals;
        }, {});
    }, [appointments]);

    const updateAppointment = async (appointmentId, action) => {
        const reason = action === 'reject' ? window.prompt('Reason for rejection:') : '';
        if (action === 'reject' && reason === null) return;

        setSavingId(appointmentId);
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
            setMessage({ text: `Appointment ${action === 'approve' ? 'approved' : 'rejected'}.`, type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || `Unable to ${action} appointment.`, type: 'error' });
        } finally {
            setSavingId(null);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    if (loading) {
        return (
            <div className="staff-page">
                <div className="staff-loading">Loading staff dashboard...</div>
            </div>
        );
    }

    if (!currentUser || !staffRoles.includes(currentUser.role)) {
        return (
            <div className="staff-page">
                <div className="staff-denied">
                    <h1>Staff Access Required</h1>
                    <p>Your account is not assigned to a staff role yet.</p>
                    <button className="staff-primary-btn" onClick={() => navigate('/')}>Back to Home</button>
                </div>
            </div>
        );
    }

    return (
        <div className="staff-page">
            <nav className="staff-nav">
                <div>
                    <div className="staff-brand">CIT-Care Staff</div>
                    <span>{serviceLabel(currentUser.role)}</span>
                </div>
                <div className="staff-nav-actions">
                    <button className="staff-secondary-btn" onClick={() => navigate('/')}>Home</button>
                    <button className="staff-text-btn" onClick={handleLogout}>Logout</button>
                </div>
            </nav>

            <main className="staff-content">
                <section className="staff-header">
                    <div>
                        <p className="staff-kicker">Appointment Review</p>
                        <h1>{dashboardTitle(currentUser.role)}</h1>
                    </div>
                    <button className="staff-primary-btn" onClick={() => fetchAppointments()}>Refresh</button>
                </section>

                {message.text && <div className={`staff-message ${message.type}`}>{message.text}</div>}

                <section className="staff-metrics">
                    {['PENDING', 'APPROVED', 'REJECTED'].map((status) => (
                        <div className="staff-metric" key={status}>
                            <span>{status}</span>
                            <strong>{counts[status] || 0}</strong>
                        </div>
                    ))}
                </section>

                <section className="staff-panel">
                    <div className="staff-panel-header">
                        <h2>Appointments</h2>
                        <input
                            type="search"
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                            placeholder="Search appointments"
                            aria-label="Search appointments"
                        />
                    </div>

                    <div className="staff-table-wrap">
                        <table className="staff-table">
                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Service</th>
                                    <th>Date</th>
                                    <th>Time</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredAppointments.length > 0 ? filteredAppointments.map((appointment) => (
                                    <tr key={appointment.id}>
                                        <td>
                                            <strong>{appointment.student?.fullName || 'Unknown student'}</strong>
                                            <span>{appointment.student?.email}</span>
                                        </td>
                                        <td>{appointment.service?.name}</td>
                                        <td>{appointment.appointmentDate}</td>
                                        <td>{appointment.appointmentTime}</td>
                                        <td><span className={`staff-status ${appointment.status?.toLowerCase()}`}>{appointment.status}</span></td>
                                        <td>
                                            <div className="staff-row-actions">
                                                <button
                                                    className="staff-approve-btn"
                                                    disabled={savingId === appointment.id || appointment.status === 'APPROVED'}
                                                    onClick={() => updateAppointment(appointment.id, 'approve')}
                                                >
                                                    Approve
                                                </button>
                                                <button
                                                    className="staff-reject-btn"
                                                    disabled={savingId === appointment.id || appointment.status === 'REJECTED'}
                                                    onClick={() => updateAppointment(appointment.id, 'reject')}
                                                >
                                                    Reject
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="6" className="staff-empty">No appointments found.</td>
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

export default StaffDashboard;
