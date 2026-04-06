package edu.cit.abella.citcare.repository;

// import edu.cit.abella.citcare.entity.Appointment;
import edu.cit.abella.citcare.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
// import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    
}