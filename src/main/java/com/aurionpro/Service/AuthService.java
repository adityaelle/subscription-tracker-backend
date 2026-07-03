package com.aurionpro.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aurionpro.DTO.AuthResponseDTO;
import com.aurionpro.DTO.LoginDTO;
import com.aurionpro.DTO.RegisterDTO;
import com.aurionpro.Repository.UserRepository;
import com.aurionpro.Security.JwtUtil;
import com.aurionpro.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ─── Register ─────────────────────────────────────────────────────────────
    public AuthResponseDTO register(RegisterDTO dto) {

        // 1. Check if email is already taken
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered: " + dto.getEmail());
        }

        // 2. Build and save the user (password is BCrypt hashed)
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                .build();

        User savedUser = userRepository.save(user);

        // 3. Generate JWT for immediate login after registration
        UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getEmail(),
                        savedUser.getPassword(),
                        java.util.Collections.emptyList()
                );

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(
                token,
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getId()
        );
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    public AuthResponseDTO login(LoginDTO dto) {

        // 1. Let Spring Security's AuthenticationManager verify credentials.
        //    This internally calls UserDetailsServiceImpl.loadUserByUsername()
        //    and checks the BCrypt password. Throws BadCredentialsException if wrong.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );

        // 2. If we reach here, credentials are valid. Load the user from DB.
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(
                token,
                user.getEmail(),
                user.getName(),
                user.getId()
        );
    }
}