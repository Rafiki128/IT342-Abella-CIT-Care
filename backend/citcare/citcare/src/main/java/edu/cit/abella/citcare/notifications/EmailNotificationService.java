package edu.cit.abella.citcare.notifications;

import edu.cit.abella.citcare.entity.Appointment;
import edu.cit.abella.citcare.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${citcare.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${citcare.mail.from:}")
    private String fromAddress;

    public void sendAppointmentUpdate(Appointment appointment) {
        if (!mailEnabled) {
            log.info("Appointment email skipped because citcare.mail.enabled is false.");
            return;
        }

        if (mailSender == null) {
            log.warn("Appointment email skipped because JavaMailSender is not configured.");
            return;
        }

        if (appointment == null) {
            log.warn("Appointment email skipped because appointment is missing.");
            return;
        }

        if (appointment.getStudent() == null) {
            log.warn("Appointment email skipped for appointment {} because student is missing.", appointment.getId());
            return;
        }

        User student = appointment.getStudent();
        if (!student.isEmailNotificationsEnabled()) {
            log.info("Appointment email skipped for appointment {} because student {} disabled email notifications.",
                    appointment.getId(), student.getId());
            return;
        }

        if (student.getEmail() == null || student.getEmail().isBlank()) {
            log.warn("Appointment email skipped for appointment {} because student {} has no email address.",
                    appointment.getId(), student.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) {
                message.setFrom(fromAddress);
            }
            message.setTo(student.getEmail());
            message.setSubject("CIT-Care appointment update: " + statusText(appointment.getStatus()));
            message.setText(buildBody(appointment));
            mailSender.send(message);
            log.info("Appointment email sent for appointment {} to {}.", appointment.getId(), student.getEmail());
        } catch (MailException error) {
            log.error("Appointment email failed for appointment {} to {}: {}",
                    appointment.getId(), student.getEmail(), error.getMessage());
        } catch (RuntimeException error) {
            // Appointment updates should not fail just because email delivery is unavailable.
            log.error("Appointment email failed for appointment {} to {}: {}",
                    appointment.getId(), student.getEmail(), error.getMessage());
        }
    }

    private String buildBody(Appointment appointment) {
        String studentName = appointment.getStudent().getFullName();
        String serviceName = appointment.getService() == null ? "your appointment" : appointment.getService().getName();
        String date = appointment.getAppointmentDate() == null ? "TBA" : appointment.getAppointmentDate().format(DATE_FORMAT);
        String time = appointment.getAppointmentTime() == null ? "TBA" : appointment.getAppointmentTime().format(TIME_FORMAT);
        String office = appointment.getOffice() == null || appointment.getOffice().isBlank() ? "CIT-Care Office" : appointment.getOffice();

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(studentName).append(",\n\n");
        body.append("Your CIT-Care appointment has been updated.\n\n");
        body.append("Status: ").append(statusText(appointment.getStatus())).append("\n");
        body.append("Service: ").append(serviceName).append("\n");
        body.append("Date: ").append(date).append("\n");
        body.append("Time: ").append(time).append("\n");
        body.append("Office: ").append(office).append("\n");

        if (appointment.getRejectionReason() != null && !appointment.getRejectionReason().isBlank()) {
            body.append("Note: ").append(appointment.getRejectionReason()).append("\n");
        }

        body.append("\nPlease sign in to CIT-Care for more details.");
        return body.toString();
    }

    private String statusText(String status) {
        if ("APPROVED".equals(status)) return "Confirmed";
        if ("REJECTED".equals(status)) return "Cancelled";
        if ("COMPLETED".equals(status)) return "Completed";
        return status == null ? "Pending" : status;
    }
}
