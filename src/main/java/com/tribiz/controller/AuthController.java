package com.tribiz.controller;

import com.tribiz.dto.request.LoginRequest;
import com.tribiz.dto.request.SignupRequest;
import com.tribiz.dto.response.JwtResponse;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Role;
import com.tribiz.entity.User;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.jwt.JwtUtils;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .gender(signUpRequest.getGender())
                .dateOfBirth(signUpRequest.getDateOfBirth())
                .location(signUpRequest.getLocation())
                .build();

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(Role.CUSTOMER);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        roles.add(Role.ADMIN);
                        break;
                    case "seller":
                        roles.add(Role.SELLER);
                        break;
                    case "service":
                        roles.add(Role.SERVICE_PROVIDER);
                        break;
                    default:
                        roles.add(Role.CUSTOMER);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/social/google")
    public void loginWithGoogle(HttpServletResponse response) throws Exception {
        String authorizationUri = "/oauth2/authorization/google";
        response.sendRedirect(authorizationUri);
    }

    @GetMapping("/social/github")
    public void loginWithGithub(HttpServletResponse response) throws Exception {
        String authorizationUri = "/oauth2/authorization/github";
        response.sendRedirect(authorizationUri);
    }

    @GetMapping("/social/callback")
    public ResponseEntity<?> handleOAuthCallback() {
        // This endpoint can be used to check OAuth login status
        // The actual OAuth callback is handled by Spring Security
        return ResponseEntity.ok(new MessageResponse("OAuth callback handled by Spring Security"));
    }
}
