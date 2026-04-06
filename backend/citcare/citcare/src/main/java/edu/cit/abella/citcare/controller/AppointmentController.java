package edu.cit.abella.citcare.controller;

import edu.cit.abella.citcare.dto.AppointmentRequest;
import edu.cit.abella.citcare.entity.Appointment;
import edu.cit.abella.citcare.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
