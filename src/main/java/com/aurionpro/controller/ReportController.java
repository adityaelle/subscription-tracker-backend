package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.DTO.ReportDTO;
import com.aurionpro.Service.ReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/summary
    @GetMapping("/summary")
    public ResponseEntity<ReportDTO> getSummary() {
        return ResponseEntity.ok(reportService.generateReport());
    }
}