import React, { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { getStoredUser } from '../../components/UniversalDashboard';
import './Home.css';

const Home = () => {
    const [user] = useState(getStoredUser);
    const navigate = useNavigate();

    if (user) {
        return <Navigate to="/dashboard" replace />;
    }

    return (
        <div className="home-page guest-mode">
            <nav className="home-nav">
                <div className="home-brand">
                    <div className="home-logo">
                        <img src="/cit-logo.png" alt="" onError={(event) => { event.currentTarget.style.display = 'none'; }} />
                        <span>C</span>
                    </div>
                    <strong>CIT-Care</strong>
                </div>
                <div className="home-nav-actions">
                    <button onClick={() => navigate('/login')}>Sign In</button>
                    <button className="home-register-btn" onClick={() => navigate('/register')}>Register</button>
                </div>
            </nav>

            <main>
                <section className="home-hero">
                    <div className="home-hero-copy">
                        <span className="home-badge">Campus Health & Wellness</span>
                        <h1>Compassionate care for every CIT student.</h1>
                        <p>Book clinic visits, request guidance support, and keep track of your appointments in one simple CIT-Care portal.</p>
                        <div className="home-hero-actions">
                            <button className="home-primary-btn" onClick={() => navigate('/login')}>Book Appointment</button>
                            <button className="home-secondary-btn" onClick={() => navigate('/register')}>Create Account</button>
                        </div>
                    </div>
                    <div className="home-hero-card" aria-label="CIT-Care appointment preview">
                        <div className="home-visual-band">
                            <div className="home-visual-logo">
                                <img src="/cit-logo.png" alt="" onError={(event) => { event.currentTarget.style.display = 'none'; }} />
                                <span>C</span>
                            </div>
                            <div>
                                <strong>CIT-Care Portal</strong>
                                <p>Clinic and guidance support</p>
                            </div>
                        </div>
                        <div className="home-card-top">
                            <span>Today</span>
                            <strong>3 open services</strong>
                        </div>
                        <div className="home-hero-item">
                            <span className="home-service-dot medical" />
                            <div>
                                <strong>Medical Clinic</strong>
                                <p>Checkups, first aid, referrals</p>
                            </div>
                        </div>
                        <div className="home-hero-item">
                            <span className="home-service-dot guidance" />
                            <div>
                                <strong>Guidance Counseling</strong>
                                <p>Mental health and academic support</p>
                            </div>
                        </div>
                        <div className="home-callout">
                            Need urgent support? Our staff can help route you to the right office.
                        </div>
                    </div>
                </section>

                <section className="home-stats" aria-label="CIT-Care highlights">
                    <div><strong>Fast</strong><span>Appointment requests</span></div>
                    <div><strong>Secure</strong><span>Student account access</span></div>
                    <div><strong>Unified</strong><span>Clinic and guidance services</span></div>
                </section>

                <section className="home-services">
                    <div className="home-section-heading">
                        <span className="home-badge">Services</span>
                        <h2>Choose the support you need</h2>
                        <p>Start with the right office and manage your visit from your dashboard.</p>
                    </div>
                    <div className="home-service-grid">
                        <article>
                            <div className="home-service-icon home-service-icon-medical" aria-hidden="true" />
                            <h3>Medical Clinic</h3>
                            <p>General checkups, medical consultations, first aid, and health referrals.</p>
                            <button onClick={() => navigate('/login')}>Book Medical Visit</button>
                        </article>
                        <article>
                            <div className="home-service-icon home-service-icon-guidance" aria-hidden="true" />
                            <h3>Guidance Office</h3>
                            <p>Counseling, wellness support, academic guidance, and career conversations.</p>
                            <button onClick={() => navigate('/login')}>Book Guidance Session</button>
                        </article>
                    </div>
                </section>

                <section className="home-process">
                    <div className="home-section-heading">
                        <span className="home-badge">How It Works</span>
                        <h2>Get support in three steps</h2>
                        <p>CIT-Care keeps the process simple from sign in to appointment updates.</p>
                    </div>
                    <div className="home-process-grid">
                        <article>
                            <span>1</span>
                            <h3>Create your account</h3>
                            <p>Register with your student details and wait for role approval if needed.</p>
                        </article>
                        <article>
                            <span>2</span>
                            <h3>Choose a service</h3>
                            <p>Select medical clinic or guidance counseling and send your request.</p>
                        </article>
                        <article>
                            <span>3</span>
                            <h3>Track updates</h3>
                            <p>View appointment status, approval notes, and upcoming schedules.</p>
                        </article>
                    </div>
                </section>

                <section className="home-support-strip">
                    <div>
                        <span className="home-badge">Need Help?</span>
                        <h2>For urgent concerns, visit the clinic or guidance office directly.</h2>
                    </div>
                    <button onClick={() => navigate('/login')}>Open CIT-Care</button>
                </section>
            </main>

            <footer className="home-footer">
                <div>
                    <strong>CIT-Care</strong>
                    <span>Your trusted partner for campus healthcare and wellness.</span>
                </div>
                <div>
                    <strong>Services</strong>
                    <span>Medical Clinic &middot; Guidance Office &middot; Wellness Support</span>
                </div>
            </footer>
        </div>
    );
};

export default Home;
