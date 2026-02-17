package com.tribiz.controller;

import com.tribiz.entity.Role;
import com.tribiz.entity.Shop;
import com.tribiz.entity.User;
import com.tribiz.repository.ShopRepository;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/staff")
@PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
public class StaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<List<User>> getMyStaff(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userOpt.get();
        List<Shop> shops = shopRepository.findByOwnerId(user.getId());
        List<User> allStaff = shops.stream()
                .flatMap(shop -> userRepository.findByShop_IdAndRolesContaining(shop.getId(), Role.STAFF).stream())
                .toList();
        return ResponseEntity.ok(allStaff);
    }

    @PostMapping("/shops/{shopId}")
    public ResponseEntity<User> addStaff(@PathVariable("shopId") Long shopId, @RequestBody User staffRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User owner = userOpt.get();
        Optional<Shop> shopOpt = shopRepository.findById(shopId);
        if (shopOpt.isEmpty() || !shopOpt.get().getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Shop shop = shopOpt.get();

        // Check if email or username already exists
        if (userRepository.existsByEmail(staffRequest.getEmail()) || userRepository.existsByUsername(staffRequest.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        User staff = User.builder()
                .firstName(staffRequest.getFirstName())
                .lastName(staffRequest.getLastName())
                .email(staffRequest.getEmail())
                .username(staffRequest.getUsername())
                .password(passwordEncoder.encode(staffRequest.getPassword()))
                .roles(Set.of(Role.STAFF))
                .shop(shop)
                .status("ACTIVE")
                .build();

        User savedStaff = userRepository.save(staff);
        return ResponseEntity.ok(savedStaff);
    }

    @PutMapping("/{staffId}")
    public ResponseEntity<User> updateStaff(@PathVariable("staffId") Long staffId, @RequestBody User staffRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> ownerOpt = userRepository.findByUsername(username);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User owner = ownerOpt.get();

        Optional<User> staffOpt = userRepository.findById(staffId);
        if (staffOpt.isEmpty() || !staffOpt.get().getRoles().contains(Role.STAFF)) {
            return ResponseEntity.badRequest().build();
        }
        User staff = staffOpt.get();

        // Check if the staff belongs to one of the owner's shops
        if (staff.getShop() == null || !staff.getShop().getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.badRequest().build();
        }

        // Update fields
        staff.setFirstName(staffRequest.getFirstName());
        staff.setLastName(staffRequest.getLastName());
        staff.setLocation(staffRequest.getLocation());
        // Don't update email, username, password here for simplicity

        User updatedStaff = userRepository.save(staff);
        return ResponseEntity.ok(updatedStaff);
    }

    @DeleteMapping("/{staffId}")
    public ResponseEntity<Void> deleteStaff(@PathVariable("staffId") Long staffId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> ownerOpt = userRepository.findByUsername(username);
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User owner = ownerOpt.get();

        Optional<User> staffOpt = userRepository.findById(staffId);
        if (staffOpt.isEmpty() || !staffOpt.get().getRoles().contains(Role.STAFF)) {
            return ResponseEntity.badRequest().build();
        }
        User staff = staffOpt.get();

        // Check if the staff belongs to one of the owner's shops
        if (staff.getShop() == null || !staff.getShop().getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.badRequest().build();
        }

        userRepository.delete(staff);
        return ResponseEntity.ok().build();
    }
}
