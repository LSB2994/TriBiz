package com.tribiz.service.impl;

import com.tribiz.dto.request.LoginRequest;
import com.tribiz.dto.request.SignupRequest;
import com.tribiz.dto.response.JwtResponse;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Role;
import com.tribiz.entity.User;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.jwt.JwtUtils;
import com.tribiz.security.services.UserDetailsImpl;
import com.tribiz.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles);
    }

    @Override
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
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
                switch (role.toLowerCase()) {
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

        return new MessageResponse("User registered successfully!");
    }

    @Override
    public Map<String, String> getSocialLoginUrl(String provider) {
        String authorizationUri;
        if ("google".equalsIgnoreCase(provider)) {
            authorizationUri = "/oauth2/authorization/google";
        } else if ("github".equalsIgnoreCase(provider)) {
            authorizationUri = "/oauth2/authorization/github";
        } else {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authorizationUri);
        return response;
    }
}
