package com.aurionpro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.DTO.AuthResponseDTO;
import com.aurionpro.DTO.LoginDTO;
import com.aurionpro.DTO.RegisterDTO;
import com.aurionpro.Service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    // Body: { "name": "Raj", "email": "raj@email.com", "password": "secret123" }
    
    @RestController
    public class TestController {

        @GetMapping("/test")
        public String test() {
            return "WORKING";
        }
    }
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterDTO dto
    ) {
        System.out.println("REGISTER API HIT");

        AuthResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/auth/login
    // Body: { "email": "raj@email.com", "password": "secret123" }
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginDTO dto
    ) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}