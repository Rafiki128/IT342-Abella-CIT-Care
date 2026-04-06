package edu.cit.abella.citcare.service;

import edu.cit.abella.citcare.dto.AppointmentRequest;
import edu.cit.abella.citcare.entity.Appointment;
import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.entity.ServiceEntity;
import edu.cit.abella.citcare.repository.AppointmentRepository;
import edu.cit.abella.citcare.repository.ServiceRepository;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServiceRepository ServiceRepository;

    public Appointment createAppointment(AppointmentRequest dto) {
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ServiceEntity service = ServiceRepository.findById(dto.getServiceId())
            .orElseThrow(() -> new RuntimeException("Service not found"));

        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setService(service);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setReason(dto.getReason());
        appointment.setOffice(dto.getOffice());
        appointment.setStatus("PENDING");

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getStudentAppointments(Long studentId) {
        return appointmentRepository.findByStudentId(studentId);
    }

    public Appointment approveAppointment(Long appointmentId, Long staffId) {
    Appointment app = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
    User staff = userRepository.findById(staffId).orElseThrow();
    
    app.setStatus("APPROVED");
    app.setStaff(staff);
    app.setApprovedAt(LocalDateTime.now());
    return appointmentRepository.save(app);
}

    public Appointment rejectAppointment(Long appointmentId, String reason) {
    Appointment app = appointmentRepository.findById(appointmentId).orElseThrow();
    app.setStatus("REJECTED");
    app.setRejectionReason(reason);
    app.setRejectedAt(LocalDateTime.now());
    return appointmentRepository.save(app);
}

}