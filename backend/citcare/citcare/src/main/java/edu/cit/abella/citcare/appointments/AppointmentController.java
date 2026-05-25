package edu.cit.abella.citcare.appointments;

import edu.cit.abella.citcare.entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // POST: http://localhost:8080/api/appointments/book
    @PostMapping("/book")
    public ResponseEntity<Appointment> book(@RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.createAppointment(request));
    }

    // GET: http://localhost:8080/api/appointments/all
    @GetMapping("/all")
    public List<Appointment> getAll() {
        return appointmentService.getAllAppointments();
    }

    // GET: http://localhost:8080/api/appointments/student/{id}
    @GetMapping("/student/{studentId}")
    public List<Appointment> getByStudent(@PathVariable Long studentId) {
        return appointmentService.getStudentAppointments(studentId);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getByStaff(@PathVariable Long staffId) {
        try {
            List<Map<String, Object>> appointments = appointmentService.getStaffAppointments(staffId).stream()
                    .map(this::appointmentResponse)
                    .toList();
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException error) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse(error.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long appointmentId, @RequestBody ApprovalRequest request) {
        try {
            return ResponseEntity.ok(appointmentResponse(
                    appointmentService.approveAppointment(appointmentId, request.getStaffId())
            ));
        } catch (RuntimeException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse(error.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long appointmentId, @RequestBody RejectionRequest request) {
        try {
            return ResponseEntity.ok(appointmentResponse(
                    appointmentService.rejectAppointment(appointmentId, request.getStaffId(), request.getReason())
            ));
        } catch (RuntimeException error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse(error.getMessage()));
        }
    }

    private Map<String, Object> appointmentResponse(Appointment appointment) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", appointment.getId());
        data.put("appointmentDate", appointment.getAppointmentDate());
        data.put("appointmentTime", appointment.getAppointmentTime());
        data.put("reason", appointment.getReason());
        data.put("status", appointment.getStatus());
        data.put("office", appointment.getOffice());
        data.put("rejectionReason", appointment.getRejectionReason());

        if (appointment.getService() != null) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", appointment.getService().getId());
            service.put("name", appointment.getService().getName());
            data.put("service", service);
        }

        if (appointment.getStudent() != null) {
            Map<String, Object> student = new HashMap<>();
            student.put("id", appointment.getStudent().getId());
            student.put("fullName", appointment.getStudent().getFullName());
            student.put("email", appointment.getStudent().getEmail());
            data.put("student", student);
        }

        if (appointment.getStaff() != null) {
            Map<String, Object> staff = new HashMap<>();
            staff.put("id", appointment.getStaff().getId());
            staff.put("fullName", appointment.getStaff().getFullName());
            data.put("staff", staff);
        }

        return data;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
