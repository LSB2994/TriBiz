package com.tribiz.security.services;

import com.tribiz.entity.Role;
import com.tribiz.entity.User;
import com.tribiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = null;
        String name = null;

        // Extract user info based on provider
        if ("google".equals(registrationId)) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        } else if ("github".equals(registrationId)) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name") != null ? oauth2User.getAttribute("name") :
                  oauth2User.getAttribute("login");
        }

        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user exists - but don't save new users
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update name if not set
            if (!StringUtils.hasText(user.getFirstName()) && StringUtils.hasText(name)) {
                String[] nameParts = name.split(" ", 2);
                user.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    user.setLastName(nameParts[1]);
                }
                userRepository.save(user); // Only save updates to existing users
            }
        } else {
            // For new OAuth users, create temporary user object but DON'T save to database
            user = new User();
            user.setEmail(email);
            user.setUsername(generateUniqueUsername(email));

            if (StringUtils.hasText(name)) {
                String[] nameParts = name.split(" ", 2);
                user.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    user.setLastName(nameParts[1]);
                }
            } else {
                user.setFirstName("OAuth2");
                user.setLastName("User");
            }

            // Set default password for OAuth2 users (they won't use it)
            user.setPassword("oauth2_user");

            // Set default role
            user.setRoles(Collections.singleton(Role.CUSTOMER));

            // NOTE: Saving new OAuth users to database
            userRepository.save(user);
        }

        return oauth2User;
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"));
        String username = baseUsername;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}
