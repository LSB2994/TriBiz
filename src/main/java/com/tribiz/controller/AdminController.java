package com.tribiz.controller;

import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Role;
import com.tribiz.entity.Shop;
import com.tribiz.entity.User;
import com.tribiz.repository.ShopRepository;
import com.tribiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalShops", shopRepository.count());
        stats.put("pendingShops", shopRepository.findAll().stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .count());
        stats.put("activeUsers", userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Remove sensitive information
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> statusMap) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        user.setStatus(statusMap.get("status"));
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User status updated successfully!"));
    }

    @GetMapping("/shops/pending")
    public ResponseEntity<List<Shop>> getPendingShops() {
        List<Shop> pending = shopRepository.findAll().stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/shops")
    public ResponseEntity<List<Shop>> getAllShops() {
        return ResponseEntity.ok(shopRepository.findAll());
    }

    @GetMapping("/shops/{id}")
    public ResponseEntity<Shop> getShopById(@PathVariable("id") Long id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Shop not found."));
        return ResponseEntity.ok(shop);
    }

    @DeleteMapping("/shops/{id}")
    public ResponseEntity<?> deleteShop(@PathVariable("id") Long id) {
        shopRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Shop deleted successfully!"));
    }

    @PutMapping("/shops/{id}/status")
    public ResponseEntity<?> updateShopStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> statusMap) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Shop not found."));
        shop.setStatus(statusMap.get("status"));
        shopRepository.save(shop);
        return ResponseEntity.ok(new MessageResponse("Shop status updated successfully!"));
    }

    // User management endpoints
    @GetMapping("/users/sellers")
    public ResponseEntity<List<User>> getAllSellers() {
        List<User> sellers = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains(Role.SELLER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/users/service-providers")
    public ResponseEntity<List<User>> getAllServiceProviders() {
        List<User> providers = userRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().contains(Role.SERVICE_PROVIDER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/users/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRoles() == null || 
                        (!u.getRoles().contains(Role.SELLER) && 
                         !u.getRoles().contains(Role.SERVICE_PROVIDER) && 
                         !u.getRoles().contains(Role.ADMIN)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") Long id, @RequestBody Map<String, String> roleMap) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        String roleStr = roleMap.get("role");
        if (roleStr != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.valueOf(roleStr));
            user.setRoles(roles);
            userRepository.save(user);
        }
        return ResponseEntity.ok(new MessageResponse("User role updated successfully!"));
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable("id") Long id, @RequestBody Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        try {
            Set<Role> roleEnums = roles.stream()
                    .map(role -> Role.valueOf(role.toUpperCase()))
                    .collect(Collectors.toSet());
            
            user.setRoles(roleEnums);
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("User roles updated successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid role: " + e.getMessage()));
        }
    }
}
