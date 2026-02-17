package com.tribiz.service;

import com.tribiz.dto.request.LoginRequest;
import com.tribiz.dto.request.SignupRequest;
import com.tribiz.dto.response.JwtResponse;
import com.tribiz.dto.response.MessageResponse;

import java.util.Map;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);

    MessageResponse registerUser(SignupRequest signUpRequest);

    Map<String, String> getSocialLoginUrl(String provider);
}
