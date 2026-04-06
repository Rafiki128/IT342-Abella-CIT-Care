package edu.cit.abella.citcare.service;

import edu.cit.abella.citcare.entity.ServiceEntity;
import edu.cit.abella.citcare.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    // Get all available offices/services for the React dropdown
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    // Admin function to add a new office (e.g. "Dental")
    public ServiceEntity saveService(ServiceEntity service) {
        // You could add logic here to check if the name already exists
        return serviceRepository.save(service);
    }
    
    // Find a specific service by ID
    public ServiceEntity getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }
}