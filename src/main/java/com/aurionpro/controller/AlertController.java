package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.DTO.AlertDTO;
import com.aurionpro.Service.AlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final JavaMailSender mailSender;

    // GET /api/alerts
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts() {
        return ResponseEntity.ok(
            alertService.getAllAlertsForCurrentUser()
        );
    }

    // GET /api/alerts/upcoming
    @GetMapping("/upcoming")
    public ResponseEntity<List<AlertDTO>> getUpcoming() {
        return ResponseEntity.ok(
            alertService.getUpcomingAlertsForCurrentUser()
        );
    }

    // GET /api/alerts/direct
    @GetMapping("/direct")
    public ResponseEntity<List<AlertDTO>> getDirect() {
        return ResponseEntity.ok(
            alertService.getDirectUpcoming()
        );
    }

    // POST /api/alerts/trigger
    // Generates alerts AND immediately sends emails
    @PostMapping("/trigger")
    public ResponseEntity<String> trigger() {
        alertService.generateAlertsForAllUsers();
        alertService.sendDueAlerts();
        return ResponseEntity.ok("Alerts generated and emails sent");
    }

    // POST /api/alerts/test-mail
    // Tests raw mail config
    @PostMapping("/test-mail")
    public ResponseEntity<String> testMail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("adityaelle9969@gmail.com");
            message.setSubject("SubTrap Test Email");
            message.setText("If you receive this, email is working!");
            mailSender.send(message);
            return ResponseEntity.ok("Test email sent!");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                   .body("Mail failed: " + e.getMessage());
        }
    }
}