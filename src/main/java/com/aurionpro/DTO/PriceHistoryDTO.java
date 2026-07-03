package com.aurionpro.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceHistoryDTO {
    private Long id;
    private Long subscriptionId;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
    private LocalDateTime changedAt;
}