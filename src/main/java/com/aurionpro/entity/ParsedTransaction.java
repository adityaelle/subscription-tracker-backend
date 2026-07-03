package com.aurionpro.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "raw_description", length = 500)
    private String rawDescription;

    @Column(name = "matched_subscription_id")
    private Long matchedSubscriptionId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(length = 20)
    private String source;   // EMAIL or BANK_CSV

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}