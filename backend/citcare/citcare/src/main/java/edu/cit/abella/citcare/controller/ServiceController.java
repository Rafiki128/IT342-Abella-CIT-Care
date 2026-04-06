package edu.cit.abella.citcare.controller;

import edu.cit.abella.citcare.entity.ServiceEntity;
import edu.cit.abella.citcare.service.ServiceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public List<ServiceEntity> list() {
        return serviceService.getAllServices();
    }

    @PostMapping("/add")
    public ServiceEntity add(@RequestBody ServiceEntity service) {
        return serviceService.saveService(service);
    }
}