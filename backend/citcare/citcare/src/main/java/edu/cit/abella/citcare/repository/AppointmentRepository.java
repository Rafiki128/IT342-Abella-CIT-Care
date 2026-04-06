package edu.cit.abella.citcare.repository;

import edu.cit.abella.citcare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Finds all appointments for a specific student
    List<Appointment> findByStudentId(Long studentId);
}