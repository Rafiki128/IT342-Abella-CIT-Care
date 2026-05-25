package edu.cit.abella.citcare.appointments;

import edu.cit.abella.citcare.entity.Appointment;
import edu.cit.abella.citcare.entity.ServiceEntity;
import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.AppointmentRepository;
import edu.cit.abella.citcare.repository.ServiceRepository;
import edu.cit.abella.citcare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createAppointmentSavesPendingAppointmentForStudentAndService() {
        User student = new User();
        student.setId(1L);
        ServiceEntity service = new ServiceEntity();
        service.setId(2L);
        service.setName("Medical Clinic");
        AppointmentRequest request = new AppointmentRequest();
        request.setStudentId(1L);
        request.setServiceId(2L);
        request.setAppointmentDate(LocalDate.of(2026, 5, 15));
        request.setAppointmentTime(LocalTime.of(9, 30));
        request.setReason("Consultation");
        request.setOffice("Clinic Room 1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(serviceRepository.findById(2L)).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment appointment = appointmentService.createAppointment(request);

        assertSame(student, appointment.getStudent());
        assertSame(service, appointment.getService());
        assertEquals("PENDING", appointment.getStatus());
        assertEquals("Clinic Room 1", appointment.getOffice());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void getStudentAppointmentsDelegatesToRepository() {
        List<Appointment> appointments = List.of(new Appointment());
        when(appointmentRepository.findByStudentId(1L)).thenReturn(appointments);

        assertSame(appointments, appointmentService.getStudentAppointments(1L));
    }

    @Test
    void approveAppointmentMarksAppointmentApprovedAndAssignsStaff() {
        Appointment appointment = new Appointment();
        ServiceEntity service = new ServiceEntity();
        service.setName("Medical Clinic");
        appointment.setService(service);
        User staff = new User();
        staff.setId(5L);
        staff.setRole("MEDICAL_STAFF");
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.approveAppointment(10L, 5L);

        assertEquals("APPROVED", result.getStatus());
        assertSame(staff, result.getStaff());
        assertNotNull(result.getApprovedAt());
    }

    @Test
    void rejectAppointmentMarksAppointmentRejectedWithReason() {
        Appointment appointment = new Appointment();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment result = appointmentService.rejectAppointment(10L, "Unavailable");

        assertEquals("REJECTED", result.getStatus());
        assertEquals("Unavailable", result.getRejectionReason());
        assertNotNull(result.getRejectedAt());
    }

    @Test
    void getStaffAppointmentsOnlyReturnsAppointmentsForStaffService() {
        User staff = new User();
        staff.setId(5L);
        staff.setRole("GUIDANCE_STAFF");

        ServiceEntity guidance = new ServiceEntity();
        guidance.setName("Guidance Office");
        Appointment guidanceAppointment = new Appointment();
        guidanceAppointment.setService(guidance);

        ServiceEntity medical = new ServiceEntity();
        medical.setName("Medical Clinic");
        Appointment medicalAppointment = new Appointment();
        medicalAppointment.setService(medical);

        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));
        when(appointmentRepository.findAll()).thenReturn(List.of(guidanceAppointment, medicalAppointment));

        List<Appointment> result = appointmentService.getStaffAppointments(5L);

        assertEquals(1, result.size());
        assertSame(guidanceAppointment, result.get(0));
    }

    @Test
    void approveAppointmentRejectsWrongStaffService() {
        ServiceEntity service = new ServiceEntity();
        service.setName("Guidance Office");
        Appointment appointment = new Appointment();
        appointment.setService(service);

        User staff = new User();
        staff.setId(5L);
        staff.setRole("MEDICAL_STAFF");

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(5L)).thenReturn(Optional.of(staff));

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> appointmentService.approveAppointment(10L, 5L));

        assertEquals("This appointment belongs to another service", error.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
