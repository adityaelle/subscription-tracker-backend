package com.aurionpro.util;

import com.aurionpro.entity.User;
import com.aurionpro.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    // Call this from any service/controller to get the logged-in User entity
    public User getLoggedInUser() {
        // SecurityContextHolder has the email (username) from the JWT
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new RuntimeException("Logged in user not found: " + email)
                );
    }

    // Returns just the userId — use this when you only need the ID
    public Long getLoggedInUserId() {
        return getLoggedInUser().getId();
    }
}