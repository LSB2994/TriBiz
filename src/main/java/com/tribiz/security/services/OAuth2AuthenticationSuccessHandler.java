package com.tribiz.security.services;

import com.tribiz.security.jwt.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Generate JWT token for the authenticated OAuth2 user
        String jwt = jwtUtils.generateTokenFromUsername(oauth2User.getAttribute("email"));

        // For API, you might want to return the token in response
        // For now, redirect to frontend with token as parameter
        String targetUrl = "http://localhost:3000/login?token=" + jwt + "&oauth2=true";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
