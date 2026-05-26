import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import UniversalDashboard, { getDisplayName, getInitials, getStoredUser } from '../../components/UniversalDashboard';
import './Profile.css';

const roleLabels = {
    NEW: 'Pending Approval',
    STUDENT: 'Student',
    MEDICAL_STAFF: 'Medical Staff',
    GUIDANCE_STAFF: 'Guidance Staff',
    ADMIN: 'Administrator'
};

const apiBaseUrl = 'http://localhost:8080';
const profilePhotoKey = (user) => `citcare-profile-photo-${user.id || user.email || 'user'}`;
const emailNotificationKey = (user) => `citcare-email-notifications-${user.id || user.email || 'user'}`;

const parseResponse = async (response) => {
    const text = await response.text();
    if (!text) return {};

    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
};

const Profile = () => {
    const navigate = useNavigate();
    const storedUser = useMemo(getStoredUser, []);
    const [user, setUser] = useState(storedUser);
    const displayName = getDisplayName(user);
    const [profilePhoto, setProfilePhoto] = useState(() => {
        if (!storedUser) return '';
        return localStorage.getItem(profilePhotoKey(storedUser)) || '';
    });
    const [emailNotifications, setEmailNotifications] = useState(() => {
        if (!storedUser) return true;
        if (typeof storedUser.emailNotificationsEnabled === 'boolean') {
            return storedUser.emailNotificationsEnabled;
        }
        return localStorage.getItem(emailNotificationKey(storedUser)) !== 'false';
    });
    const [message, setMessage] = useState({ text: '', type: '' });
    const [saving, setSaving] = useState(false);
    const [isEditingProfile, setIsEditingProfile] = useState(false);
    const [editForm, setEditForm] = useState({
        fullName: storedUser?.fullName || '',
        email: storedUser?.email || '',
        phoneNumber: storedUser?.phoneNumber || ''
    });
    const [passwordModal, setPasswordModal] = useState({ isOpen: false, currentPassword: '', newPassword: '', confirmPassword: '' });

    const handlePhotoChange = (event) => {
        const file = event.target.files?.[0];
        if (!file || !file.type.startsWith('image/')) return;

        const reader = new FileReader();
        reader.onload = () => {
            const imageData = reader.result;
            setProfilePhoto(imageData);
            localStorage.setItem(profilePhotoKey(user), imageData);
        };
        reader.readAsDataURL(file);
    };

    const handlePhotoRemove = () => {
        setProfilePhoto('');
        localStorage.removeItem(profilePhotoKey(user));
    };

    const startProfileEdit = () => {
        setEditForm({
            fullName: user.fullName || displayName,
            email: user.email || '',
            phoneNumber: user.phoneNumber || ''
        });
        setIsEditingProfile(true);
        setMessage({ text: '', type: '' });
    };

    const cancelProfileEdit = () => {
        if (!saving) setIsEditingProfile(false);
    };

    const handleProfileSave = async (event) => {
        event.preventDefault();
        const fullName = editForm.fullName.trim();
        const email = editForm.email.trim();
        const phoneNumber = editForm.phoneNumber.trim();

        if (!fullName || !email) {
            setMessage({ text: 'Full name and email are required.', type: 'error' });
            return;
        }

        setSaving(true);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/v1/auth/profile/${user.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullName, email, phoneNumber })
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Unable to update profile.');
            }

            const updatedUser = result.data?.user || { ...user, fullName, email, phoneNumber };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));
            setIsEditingProfile(false);
            setMessage({ text: 'Profile updated successfully.', type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Unable to update profile.', type: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const closePasswordModal = () => {
        if (!saving) setPasswordModal({ isOpen: false, currentPassword: '', newPassword: '', confirmPassword: '' });
    };

    const handlePasswordSave = async (event) => {
        event.preventDefault();

        if (passwordModal.newPassword !== passwordModal.confirmPassword) {
            setMessage({ text: 'New password and confirmation do not match.', type: 'error' });
            return;
        }

        setSaving(true);
        setMessage({ text: '', type: '' });

        try {
            const response = await fetch(`${apiBaseUrl}/api/v1/auth/profile/${user.id}/password`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    currentPassword: passwordModal.currentPassword,
                    newPassword: passwordModal.newPassword
                })
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Unable to change password.');
            }

            setPasswordModal({ isOpen: false, currentPassword: '', newPassword: '', confirmPassword: '' });
            setMessage({ text: result.message || 'Password updated successfully.', type: 'success' });
        } catch (error) {
            setMessage({ text: error.message || 'Unable to change password.', type: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const handleEmailNotificationToggle = async () => {
        const nextValue = !emailNotifications;
        setEmailNotifications(nextValue);
        localStorage.setItem(emailNotificationKey(user), String(nextValue));

        try {
            const response = await fetch(`${apiBaseUrl}/api/v1/auth/profile/${user.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ emailNotificationsEnabled: String(nextValue) })
            });
            const result = await parseResponse(response);

            if (!response.ok) {
                throw new Error(result.error?.message || result.message || 'Unable to update notification preference.');
            }

            const updatedUser = result.data?.user || { ...user, emailNotificationsEnabled: nextValue };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));
            setMessage({ text: `Email notifications ${nextValue ? 'enabled' : 'disabled'}.`, type: 'success' });
        } catch (error) {
            const previousValue = !nextValue;
            setEmailNotifications(previousValue);
            localStorage.setItem(emailNotificationKey(user), String(previousValue));
            setMessage({ text: error.message || 'Unable to update notification preference.', type: 'error' });
        }
    };

    if (!user) {
        return (
            <div className="profile-page">
                <section className="profile-empty">
                    <h1>Sign in required</h1>
                    <p>Please sign in to view your CIT-Care profile.</p>
                    <button className="profile-primary-btn" onClick={() => navigate('/login')}>Go to Login</button>
                </section>
            </div>
        );
    }

    return (
        <UniversalDashboard
            user={user}
            title="CIT-Care"
            subtitle="Account Profile"
            pageClassName="profile-page"
            contentClassName="profile-content"
            showProfileLink={false}
        >
                {message.text && <div className={`profile-message ${message.type}`}>{message.text}</div>}
                <section className="profile-card">
                    <div className="profile-cover" />
                    <div className="profile-card-body">
                        <label className="profile-avatar" htmlFor="profilePhoto" aria-label="Change profile photo">
                            {profilePhoto ? (
                                <img src={profilePhoto} alt="" />
                            ) : (
                                getInitials(displayName)
                            )}
                            <span className="profile-avatar-overlay">Change</span>
                        </label>
                        <div className="profile-heading-row">
                            <div className="profile-title">
                                <h1>{displayName}</h1>
                                <div className="profile-badges">
                                    <span className={`profile-role ${user.role?.toLowerCase()}`}>{roleLabels[user.role] || user.role || 'User'} Account</span>
                                    <span className="profile-status">{user.role === 'NEW' ? 'Pending approval' : 'Verified'}</span>
                                </div>
                            </div>
                            {!isEditingProfile && (
                                <button className="profile-edit-btn" type="button" onClick={startProfileEdit}>Edit Profile</button>
                            )}
                        </div>
                        <input id="profilePhoto" type="file" accept="image/*" onChange={handlePhotoChange} />
                        {profilePhoto && (
                            <button className="profile-photo-remove" type="button" onClick={handlePhotoRemove}>Remove Photo</button>
                        )}

                        <div className="profile-info-grid">
                            <section>
                                <h2>Personal Information</h2>
                                {isEditingProfile ? (
                                    <form className="profile-inline-form" onSubmit={handleProfileSave}>
                                        <label htmlFor="profileFullName">Full Name</label>
                                        <input
                                            id="profileFullName"
                                            type="text"
                                            value={editForm.fullName}
                                            onChange={(event) => setEditForm((form) => ({ ...form, fullName: event.target.value }))}
                                            required
                                            autoFocus
                                        />
                                        <label htmlFor="profileEmail">Email Address</label>
                                        <input
                                            id="profileEmail"
                                            type="email"
                                            value={editForm.email}
                                            onChange={(event) => setEditForm((form) => ({ ...form, email: event.target.value }))}
                                            required
                                        />
                                        <label htmlFor="profilePhoneNumber">Phone Number</label>
                                        <input
                                            id="profilePhoneNumber"
                                            type="tel"
                                            value={editForm.phoneNumber}
                                            onChange={(event) => setEditForm((form) => ({ ...form, phoneNumber: event.target.value }))}
                                            placeholder="Add phone number"
                                        />
                                        <div className="profile-inline-actions">
                                            <button type="button" className="profile-secondary-btn" onClick={cancelProfileEdit}>Cancel</button>
                                            <button type="submit" className="profile-primary-btn" disabled={saving}>Save Profile</button>
                                        </div>
                                    </form>
                                ) : (
                                    <>
                                        <ProfileItem label="Full Name" value={displayName} />
                                        <ProfileItem label="Email Address" value={user.email || 'Not provided'} badge="Verified" />
                                        <ProfileItem label="Phone Number" value={user.phoneNumber || 'Not provided'} />
                                    </>
                                )}
                            </section>
                            <section>
                                <h2>Account Information</h2>
                                <ProfileItem label="Role" value={roleLabels[user.role] || user.role || 'User'} />
                                <ProfileItem label="Status" value={user.role === 'NEW' ? 'Pending approval' : 'Active'} />
                                <ProfileItem label="Joined Date" value="Available soon" />
                            </section>
                        </div>
                    </div>
                </section>

                <div className="profile-settings-grid">
                    <section className="profile-settings-card">
                        <h2>Account Security</h2>
                        <button className="profile-setting-row profile-row-button" type="button" onClick={() => {
                            setPasswordModal({ isOpen: true, currentPassword: '', newPassword: '', confirmPassword: '' });
                            setMessage({ text: '', type: '' });
                        }}>
                            <span>Change Password</span><strong>Update</strong>
                        </button>
                    </section>
                    <section className="profile-settings-card">
                        <h2>Notification Preferences</h2>
                        <button className="profile-toggle-row profile-row-button" type="button" onClick={handleEmailNotificationToggle}>
                            <span>Email Notifications</span><b className={`toggle ${emailNotifications ? 'on' : ''}`} />
                        </button>
                    </section>
                </div>

                {passwordModal.isOpen && (
                    <PasswordModal
                        modal={passwordModal}
                        saving={saving}
                        onChange={(field, value) => setPasswordModal((modal) => ({ ...modal, [field]: value }))}
                        onCancel={closePasswordModal}
                        onSubmit={handlePasswordSave}
                    />
                )}
        </UniversalDashboard>
    );
};

const ProfileItem = ({ label, value, badge }) => (
    <div className="profile-info-item">
        <span>{label}</span>
        <strong>{value}</strong>
        {badge && <em>{badge}</em>}
    </div>
);

const PasswordModal = ({ modal, saving, onChange, onCancel, onSubmit }) => (
    <div className="profile-modal-backdrop" role="presentation">
        <section className="profile-modal" role="dialog" aria-modal="true" aria-labelledby="change-password-title">
            <form onSubmit={onSubmit}>
                <div className="profile-modal-header">
                    <h2 id="change-password-title">Change Password</h2>
                    <button type="button" onClick={onCancel} aria-label="Close change password modal">x</button>
                </div>
                <label htmlFor="currentPassword">Current Password</label>
                <input
                    id="currentPassword"
                    type="password"
                    value={modal.currentPassword}
                    onChange={(event) => onChange('currentPassword', event.target.value)}
                    required
                    autoFocus
                />
                <label htmlFor="newPassword">New Password</label>
                <input
                    id="newPassword"
                    type="password"
                    value={modal.newPassword}
                    onChange={(event) => onChange('newPassword', event.target.value)}
                    minLength="6"
                    required
                />
                <label htmlFor="confirmPassword">Confirm New Password</label>
                <input
                    id="confirmPassword"
                    type="password"
                    value={modal.confirmPassword}
                    onChange={(event) => onChange('confirmPassword', event.target.value)}
                    minLength="6"
                    required
                />
                <div className="profile-modal-actions">
                    <button type="button" className="profile-secondary-btn" onClick={onCancel}>Cancel</button>
                    <button type="submit" className="profile-primary-btn" disabled={saving}>Save Password</button>
                </div>
            </form>
        </section>
    </div>
);

export default Profile;
