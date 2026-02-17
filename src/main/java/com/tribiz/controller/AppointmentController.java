package com.tribiz.controller;

import com.tribiz.dto.request.AppointmentRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Appointment;
import com.tribiz.entity.ServiceItem;
import com.tribiz.entity.User;
import com.tribiz.repository.AppointmentRepository;
import com.tribiz.repository.ServiceItemRepository;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    ServiceItemRepository serviceItemRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentRequest appointmentRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User customer = userRepository.findById(userDetails.getId()).orElseThrow();

        ServiceItem serviceItem = serviceItemRepository.findById(appointmentRequest.getServiceId())
                .orElseThrow(() -> new RuntimeException("Error: Service not found!"));

        Appointment appointment = Appointment.builder()
                .customer(customer)
                .service(serviceItem)
                .shop(serviceItem.getShop())
                .appointmentTime(appointmentRequest.getAppointmentTime())
                .note(appointmentRequest.getNote())
                .status("PENDING")
                .build();

        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new MessageResponse("Appointment booked successfully!"));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return ResponseEntity.ok(appointmentRepository.findByCustomerId(userDetails.getId()));
    }

    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('SERVICE_PROVIDER') or hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getShopAppointments(@PathVariable Long shopId) {
        // TODO: Verify ownership if needed, but for now assuming valid access based on
        // role
        return ResponseEntity.ok(appointmentRepository.findByShopId(shopId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Appointment not found!"));

        // Check ownership
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!appointment.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this shop!"));
        }

        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(new MessageResponse("Appointment status updated to " + status));
    }
}
