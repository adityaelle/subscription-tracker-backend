package com.aurionpro.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank
    private String billingCycle;    // MONTHLY, YEARLY, WEEKLY

    @NotNull
    private LocalDate nextBillingDate;

    private String status;

    private Boolean isTrial;

    private LocalDate trialEndDate;

    private String websiteUrl;

    private String notes;

    private LocalDate lastUsedDate;

    private LocalDateTime createdAt;
}