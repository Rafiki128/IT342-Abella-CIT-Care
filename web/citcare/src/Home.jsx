import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BookingModal from './components/BookingModal';
import './Home.css';

const Home = () => {
    const [user, setUser] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [appointments, setAppointments] = useState([]);
    const [services, setServices] = useState([]);
    const [formData, setFormData] = useState({ serviceId: '', appointmentDate: '', appointmentTime: '', reason: '', office: '' });
    const navigate = useNavigate();

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            const parsedUser = JSON.parse(userData);
            setUser(parsedUser);
            fetchUserAppointments(parsedUser.id);
        }
        
        fetch('http://localhost:8080/api/services')
            .then(res => res.json())
            .then(data => setServices(data))
            .catch(() => console.log("Backend offline"));
    }, []);

    const fetchUserAppointments = async (userId) => {
        try {
            const res = await fetch(`http://localhost:8080/api/appointments/student/${userId}`);
            if (res.ok) setAppointments(await res.json());
        } catch (err) { console.error(err); }
    };

    const handleLogout = () => {
        localStorage.clear();
        window.location.reload();
    };

    const handleInputChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

    const handleBooking = async (e) => {
        e.preventDefault();
        const res = await fetch('http://localhost:8080/api/appointments/book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...formData, studentId: user.id })
        });
        if (res.ok) {
            alert("Booking Successful!");
            setIsModalOpen(false);
            fetchUserAppointments(user.id);
        }
    };

    // --- VIEW 1: GUEST MODE (Marketing Design) ---
    if (!user) {
        return (
            <div className="home-page guest-mode">
                <header className="hero-section">
                    <div className="hero-overlay">
                        <div className="hero-container">
                            <span className="badge">YOUR HEALTH & WELLNESS PARTNER</span>
                            <h1>Compassionate Care, <br/><span>Modern Solutions</span></h1>
                            <p>Access quality healthcare and professional guidance counseling services all in one place. Book appointments and get the support you need.</p>
                            <div className="hero-btns">
                                <button className="btn-gold" onClick={() => navigate('/login')}>Book Appointment Now →</button>
                                <button className="btn-outline">Learn More</button>
                            </div>
                            <div className="hero-stats">
                                <div className="stat"><h3>10K+</h3><p>Patients Served</p></div>
                                <div className="stat"><h3>50+</h3><p>Healthcare Professionals</p></div>
                                <div className="stat"><h3>98%</h3><p>Satisfaction Rate</p></div>
                            </div>
                        </div>
                    </div>
                </header>

                <section className="services-preview">
                    <h2>Book Your Appointment</h2>
                    <p>Choose the service you need and select your preferred slot at your convenience.</p>
                    <div className="service-grid">
                        <div className="service-card">
                            <div className="icon-med">🩺</div>
                            <h3>Medical Clinic</h3>
                            <p>Check-ups, first aid, and medical consultations.</p>
                            <button className="btn-maroon" onClick={() => navigate('/login')}>Book Now</button>
                        </div>
                        <div className="service-card">
                            <div className="icon-guidance">🧠</div>
                            <h3>Guidance Office</h3>
                            <p>Mental health support and career counseling.</p>
                            <button className="btn-maroon" onClick={() => navigate('/login')}>Book Now</button>
                        </div>
                    </div>
                </section>
            </div>
        );
    }

    // --- VIEW 2: REGISTERED MODE (Simple Dashboard) ---
    return (
        <div className="home-page user-mode">
            <nav className="dashboard-nav">
                <div className="nav-brand">CIT-Care</div>
                <div className="nav-user">
                    <span>{user.fullName}</span>
                    <button onClick={handleLogout} className="btn-text">Logout</button>
                </div>
            </nav>

            <div className="dashboard-content">
                <div className="welcome-banner">
                    <h1>Welcome Back, <span>{user.fullName}</span></h1>
                    <button className="btn-gold" onClick={() => setIsModalOpen(true)}>+ New Appointment</button>
                </div>

                <div className="table-container">
                    <h3>Your Scheduled Appointments</h3>
                    <table className="appointment-table">
                        <thead>
                            <tr>
                                <th>Service</th>
                                <th>Date</th>
                                <th>Time</th>
                                <th>Location</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {appointments.length > 0 ? appointments.map(app => (
                                <tr key={app.id}>
                                    <td><strong>{app.service?.name}</strong></td>
                                    <td>{app.appointmentDate}</td>
                                    <td>{app.appointmentTime}</td>
                                    <td>{app.office}</td>
                                    <td><span className={`status-pill ${app.status.toLowerCase()}`}>{app.status}</span></td>
                                </tr>
                            )) : (
                                <tr><td colSpan="5" className="empty">No appointments found.</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            <BookingModal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)}
                services={services}
                formData={formData}
                handleInputChange={handleInputChange}
                handleBooking={handleBooking}
            />
        </div>
    );
};

export default Home;