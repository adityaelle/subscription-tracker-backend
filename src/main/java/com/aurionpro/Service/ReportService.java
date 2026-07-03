package com.aurionpro.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.aurionpro.DTO.ReportDTO;
import com.aurionpro.Repository.SubscriptionRepository;
import com.aurionpro.entity.Subscription;
import com.aurionpro.entity.User;
import com.aurionpro.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SubscriptionRepository subscriptionRepository;
    private final SecurityUtils securityUtils;

    public ReportDTO generateReport() {
        User user = securityUtils.getLoggedInUser();
        List<Subscription> all = subscriptionRepository
                .findByUserId(user.getId());

        List<Subscription> active = all.stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .collect(Collectors.toList());

        ReportDTO report = new ReportDTO();

        // ── Total monthly spend ───────────────────────────────────────────────
        BigDecimal monthlyTotal = active.stream()
                .map(sub -> switch (sub.getBillingCycle()) {
                    case "MONTHLY" -> sub.getAmount();
                    case "YEARLY"  ->
                        sub.getAmount().divide(
                            BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP
                        );
                    case "WEEKLY"  -> sub.getAmount()
                        .multiply(BigDecimal.valueOf(4.33));
                    default -> BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setTotalMonthlySpend(monthlyTotal);
        report.setTotalYearlySpend(
            monthlyTotal.multiply(BigDecimal.valueOf(12))
                        .setScale(2, RoundingMode.HALF_UP)
        );

        // ── Spend by category ─────────────────────────────────────────────────
        Map<String, BigDecimal> byCategory = active.stream()
                .collect(Collectors.groupingBy(
                    sub -> sub.getCategory() != null
                        ? sub.getCategory() : "UNCATEGORIZED",
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Subscription::getAmount,
                        BigDecimal::add
                    )
                ));
        report.setSpendByCategory(byCategory);

        // ── Count by status ───────────────────────────────────────────────────
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(
                    Subscription::getStatus,
                    Collectors.counting()
                ));
        report.setCountByStatus(byStatus);

        // ── Active trials count ───────────────────────────────────────────────
        long trialCount = active.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsTrial()))
                .count();
        report.setActiveTrialsCount(trialCount);

        // ── Most expensive subscription ───────────────────────────────────────
        active.stream()
                .max((a, b) -> a.getAmount().compareTo(b.getAmount()))
                .ifPresent(sub -> {
                    report.setMostExpensiveName(sub.getName());
                    report.setMostExpensiveAmount(sub.getAmount());
                });

        return report;
    }
}