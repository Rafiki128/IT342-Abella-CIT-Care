package edu.cit.abella.citcare.appointments;

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

    public List<Appointment> getStaffAppointments(Long staffId) {
        User staff = getStaffUser(staffId);
        String staffRole = staff.getRole();

        return appointmentRepository.findAll().stream()
                .filter(appointment -> canManageService(staffRole, appointment.getService()))
                .toList();
    }

    public Appointment approveAppointment(Long appointmentId, Long staffId) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        User staff = getStaffUser(staffId);
        ensureStaffCanManage(staff, app);

        app.setStatus("APPROVED");
        app.setStaff(staff);
        app.setApprovedAt(LocalDateTime.now());
        app.setRejectedAt(null);
        app.setRejectionReason(null);
        return appointmentRepository.save(app);
    }

    public Appointment rejectAppointment(Long appointmentId, Long staffId, String reason) {
        Appointment app = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        User staff = getStaffUser(staffId);
        ensureStaffCanManage(staff, app);

        app.setStatus("REJECTED");
        app.setStaff(staff);
        app.setRejectionReason(reason == null || reason.isBlank() ? "Rejected by staff" : reason.trim());
        app.setRejectedAt(LocalDateTime.now());
        app.setApprovedAt(null);
        return appointmentRepository.save(app);
    }

    public Appointment rejectAppointment(Long appointmentId, String reason) {
        Appointment app = appointmentRepository.findById(appointmentId).orElseThrow();
        app.setStatus("REJECTED");
        app.setRejectionReason(reason);
        app.setRejectedAt(LocalDateTime.now());
        return appointmentRepository.save(app);
    }

    private User getStaffUser(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (!"MEDICAL_STAFF".equals(staff.getRole()) && !"GUIDANCE_STAFF".equals(staff.getRole())) {
            throw new RuntimeException("Staff access required");
        }
        return staff;
    }

    private void ensureStaffCanManage(User staff, Appointment appointment) {
        if (!canManageService(staff.getRole(), appointment.getService())) {
            throw new RuntimeException("This appointment belongs to another service");
        }
    }

    private boolean canManageService(String staffRole, ServiceEntity service) {
        if (service == null || service.getName() == null) {
            return false;
        }

        String serviceName = service.getName().toLowerCase();
        if ("MEDICAL_STAFF".equals(staffRole)) {
            return serviceName.contains("medical") || serviceName.contains("clinic");
        }
        if ("GUIDANCE_STAFF".equals(staffRole)) {
            return serviceName.contains("guidance") || serviceName.contains("counsel");
        }
        return false;
    }

}
