package edu.cit.abella.citcare.services;

import edu.cit.abella.citcare.entity.ServiceEntity;
import edu.cit.abella.citcare.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void getAllServicesReturnsRepositoryServices() {
        List<ServiceEntity> services = List.of(new ServiceEntity());
        when(serviceRepository.findAll()).thenReturn(services);

        assertSame(services, serviceService.getAllServices());
    }

    @Test
    void saveServicePersistsService() {
        ServiceEntity service = new ServiceEntity();
        service.setName("Guidance Office");
        when(serviceRepository.save(service)).thenReturn(service);

        assertSame(service, serviceService.saveService(service));
    }

    @Test
    void getServiceByIdReturnsExistingService() {
        ServiceEntity service = new ServiceEntity();
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        assertSame(service, serviceService.getServiceById(1L));
    }

    @Test
    void getServiceByIdThrowsWhenMissing() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () -> serviceService.getServiceById(1L));

        assertEquals("Service not found", error.getMessage());
    }
}
