package com.aurionpro.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    // Total monthly spend across all active subscriptions
    private BigDecimal totalMonthlySpend;

    // Total yearly spend (monthly * 12 + yearly subs)
    private BigDecimal totalYearlySpend;

    // Spend broken down by category
    // e.g. { "STREAMING": 649.00, "SAAS": 352.50, "FITNESS": 2000.00 }
    private Map<String, BigDecimal> spendByCategory;

    // Count of subscriptions by status
    // e.g. { "ACTIVE": 4, "CANCELLED": 1 }
    private Map<String, Long> countByStatus;

    // Number of active trials
    private Long activeTrialsCount;

    // Most expensive subscription name + amount
    private String mostExpensiveName;
    private BigDecimal mostExpensiveAmount;
}