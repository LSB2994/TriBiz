package com.tribiz.controller;

import com.tribiz.dto.request.LoginRequest;
import com.tribiz.dto.request.SignupRequest;
import com.tribiz.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }

    @GetMapping("/social/google")
    public ResponseEntity<?> loginWithGoogle() {
        return ResponseEntity.ok(authService.getSocialLoginUrl("google"));
    }

    @GetMapping("/social/github")
    public ResponseEntity<?> loginWithGithub() {
        return ResponseEntity.ok(authService.getSocialLoginUrl("github"));
    }

    // Keep this for now if needed, but the main flow is handled by Spring Security
    // filters
    // This endpoint was likely a placeholder or for debugging
    /*
     * @GetMapping("/social/callback")
     * public ResponseEntity<?> handleOAuthCallback() {
     * return ResponseEntity.ok(new
     * MessageResponse("OAuth callback handled by Spring Security"));
     * }
     */
}
