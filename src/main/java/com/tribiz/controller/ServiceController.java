package com.tribiz.controller;

import com.tribiz.dto.request.ServiceRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.ServiceItem;
import com.tribiz.entity.Shop;
import com.tribiz.repository.ServiceItemRepository;
import com.tribiz.repository.ShopRepository;
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
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    ServiceItemRepository serviceItemRepository;

    @Autowired
    ShopRepository shopRepository;

    @PostMapping
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> addService(@Valid @RequestBody ServiceRequest serviceRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        // Use the first shop for now - in a real app, you might want to let the user choose which shop
        Shop shop = userShops.get(0);

        ServiceItem serviceItem = ServiceItem.builder()
                .name(serviceRequest.getName())
                .description(serviceRequest.getDescription())
                .price(serviceRequest.getPrice())
                .durationMinutes(serviceRequest.getDurationMinutes())
                .status("AVAILABLE")
                .shop(shop)
                .build();

        serviceItemRepository.save(serviceItem);
        return ResponseEntity.ok(new MessageResponse("Service added successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> updateService(@PathVariable("id") Long id, @Valid @RequestBody ServiceRequest serviceRequest) {
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Service not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!serviceItem.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this service!"));
        }

        serviceItem.setName(serviceRequest.getName());
        serviceItem.setDescription(serviceRequest.getDescription());
        serviceItem.setPrice(serviceRequest.getPrice());
        serviceItem.setDurationMinutes(serviceRequest.getDurationMinutes());

        serviceItemRepository.save(serviceItem);
        return ResponseEntity.ok(new MessageResponse("Service updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> deleteService(@PathVariable("id") Long id) {
        ServiceItem serviceItem = serviceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Service not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!serviceItem.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this service!"));
        }

        serviceItemRepository.delete(serviceItem);
        return ResponseEntity.ok(new MessageResponse("Service deleted successfully!"));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ServiceItem>> getServicesByShop(@PathVariable("shopId") Long shopId) {
        return ResponseEntity.ok(serviceItemRepository.findByShopId(shopId));
    }

    @GetMapping
    public ResponseEntity<List<ServiceItem>> getAllServices() {
        return ResponseEntity.ok(serviceItemRepository.findAll());
    }
}
