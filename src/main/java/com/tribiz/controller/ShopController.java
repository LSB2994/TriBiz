package com.tribiz.controller;

import com.tribiz.dto.request.ShopRequest;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Shop;
import com.tribiz.entity.User;
import com.tribiz.repository.ShopRepository;
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
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> createShop(@Valid @RequestBody ShopRequest shopRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // Users can now have multiple shops, so we don't need this check
        // List<Shop> userShops = shopRepository.findByOwnerId(user.getId());
        // if (!userShops.isEmpty()) {
        //     return ResponseEntity.badRequest().body(new MessageResponse("Error: User already has a shop!"));
        // }

        Shop shop = Shop.builder()
                .name(shopRequest.getName())
                .location(shopRequest.getLocation())
                .contactInfo(shopRequest.getContactInfo())
                .isOpen(true)
                .owner(user)
                .build();

        shopRepository.save(shop);
        return ResponseEntity.ok(new MessageResponse("Shop created successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateShop(@PathVariable("id") Long id, @Valid @RequestBody ShopRequest shopRequest) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Shop not found."));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!shop.getOwner().getId().equals(userDetails.getId())) { // Simple check, in real app handle ADMIN too
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not the owner of this shop!"));
        }

        shop.setName(shopRequest.getName());
        shop.setLocation(shopRequest.getLocation());
        shop.setContactInfo(shopRequest.getContactInfo());
        shopRepository.save(shop);

        return ResponseEntity.ok(new MessageResponse("Shop updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteShop(@PathVariable("id") Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Shop not found."));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!shop.getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You are not the owner of this shop!"));
        }

        shopRepository.delete(shop);
        return ResponseEntity.ok(new MessageResponse("Shop deleted successfully!"));
    }

    @GetMapping
    public ResponseEntity<?> getAllShops() {
        return ResponseEntity.ok(shopRepository.findAll());
    }

    @GetMapping("/my-shop")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyShop() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("No shops found for this user."));
        }
        
        // Return all shops for the user
        return ResponseEntity.ok(userShops);
    }
}
