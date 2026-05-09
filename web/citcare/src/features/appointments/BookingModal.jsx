import React from 'react';
import './BookingModal.css';

const BookingModal = ({ isOpen, onClose, services, formData, handleInputChange, handleBooking }) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <div className="modal-header">
                    <div className="header-title">
                        <span className="icon">📅</span>
                        <div>
                            <h3>Schedule Appointment</h3>
                            <p>Choose your preferred date and time</p>
                        </div>
                    </div>
                    <button className="close-btn" onClick={onClose}>&times;</button>
                </div>
                
                <form onSubmit={handleBooking} className="modal-body">
                    <div className="booking-grid">
                        <div className="form-group full">
                            <label>Service Type</label>
                            <select name="serviceId" value={formData.serviceId} onChange={handleInputChange} required>
                                <option value="">Select a service...</option>
                                {services.map(s => (
                                    <option key={s.id} value={s.id}>{s.name}</option>
                                ))}
                            </select>
                        </div>
                        
                        <div className="form-group">
                            <label>Date</label>
                            <input type="date" name="appointmentDate" value={formData.appointmentDate} onChange={handleInputChange} required />
                        </div>

                        <div className="form-group">
                            <label>Time</label>
                            <input type="time" name="appointmentTime" value={formData.appointmentTime} onChange={handleInputChange} required />
                        </div>

                        <div className="form-group full">
                            <label>Room / Location</label>
                            <input type="text" name="office" placeholder="e.g. Room 204, Medical Building" value={formData.office} onChange={handleInputChange} required />
                        </div>

                        <div className="form-group full">
                            <label>Notes</label>
                            <textarea name="reason" placeholder="Please describe your symptoms..." value={formData.reason} onChange={handleInputChange}></textarea>
                        </div>
                    </div>

                    <div className="modal-footer">
                        <button type="button" className="btn-cancel" onClick={onClose}>Cancel</button>
                        <button type="submit" className="btn-confirm">Confirm Appointment</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default BookingModal;