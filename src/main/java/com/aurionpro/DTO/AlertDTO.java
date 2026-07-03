package com.aurionpro.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AlertDTO {
    private Long id;
    private Long subscriptionId;
    private String subscriptionName;
    private String alertType;       // RENEWAL_REMINDER, TRIAL_ENDING, PRICE_INCREASE
    private LocalDate alertDate;
    private Boolean isSent;
    private BigDecimal amount;
    private String billingCycle;
}