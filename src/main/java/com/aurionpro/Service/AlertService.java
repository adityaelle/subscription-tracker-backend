package com.aurionpro.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.DTO.AlertDTO;
import com.aurionpro.Repository.AlertRepository;
import com.aurionpro.Repository.SubscriptionRepository;
import com.aurionpro.entity.Alert;
import com.aurionpro.entity.Subscription;
import com.aurionpro.entity.User;
import com.aurionpro.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SecurityUtils securityUtils;
    private final JavaMailSender mailSender;

    // ── Get all unsent alerts for logged-in user ──────────────────────────────
    public List<AlertDTO> getUpcomingAlertsForCurrentUser() {
        User user = securityUtils.getLoggedInUser();
        return alertRepository
                .findByUserIdAndIsSentFalseOrderByAlertDateAsc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get ALL alerts for logged-in user ─────────────────────────────────────
    public List<AlertDTO> getAllAlertsForCurrentUser() {
        User user = securityUtils.getLoggedInUser();
        return alertRepository
                .findByUserIdOrderByAlertDateAsc(user.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Called by scheduler: generate alerts for upcoming renewals/trials ─────
    @Transactional
    public void generateAlertsForAllUsers() {
        LocalDate today = LocalDate.now();

        // Renewals in 7 days
        List<Subscription> renewalsIn7 = subscriptionRepository
                .findAllUpcomingRenewals(today.plusDays(7));
        renewalsIn7.forEach(sub ->
            createAlertIfNotExists(sub, "RENEWAL_REMINDER", today)
        );

        // Renewals in 3 days
        List<Subscription> renewalsIn3 = subscriptionRepository
                .findAllUpcomingRenewals(today.plusDays(3));
        renewalsIn3.forEach(sub ->
            createAlertIfNotExists(sub, "RENEWAL_REMINDER", today)
        );

        // Trials ending in 2 days
        List<Subscription> trialsEnding = subscriptionRepository
                .findTrialsEndingSoon(today.plusDays(2));
        trialsEnding.forEach(sub ->
            createAlertIfNotExists(sub, "TRIAL_ENDING", today)
        );

        log.info("Alert generation complete for date: {}", today);
    }
    // ── Called by scheduler: send all unsent alerts due today ─────────────────
    @Transactional
    public void sendDueAlerts() {
        LocalDate today = LocalDate.now();
        List<Alert> dueAlerts = alertRepository
                .findByAlertDateAndIsSentFalse(today);

        for (Alert alert : dueAlerts) {
            try {
                sendAlertEmail(alert);
                alert.setIsSent(true);
                alert.setSentAt(LocalDateTime.now());
                alertRepository.save(alert);
                log.info("Alert sent for subscription: {} to user: {}",
                        alert.getSubscription().getName(),
                        alert.getUser().getEmail());
            } catch (Exception e) {
                log.error("Failed to send alert id {}: {}",
                        alert.getId(), e.getMessage());
            }
        }
    }

    // ── Send email for a single alert ─────────────────────────────────────────
    private void sendAlertEmail(Alert alert) {
        Subscription sub = alert.getSubscription();
        User user = alert.getUser();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(buildEmailSubject(alert));
        message.setText(buildEmailBody(alert, sub, user));

        mailSender.send(message);
    }

    // ── Build email subject ───────────────────────────────────────────────────
    private String buildEmailSubject(Alert alert) {
        return switch (alert.getAlertType()) {
            case "RENEWAL_REMINDER" ->
                "Reminder: " + alert.getSubscription().getName()
                + " renews on " + alert.getAlertDate();
            case "TRIAL_ENDING" ->
                "Trial Ending: " + alert.getSubscription().getName()
                + " trial ends on " + alert.getAlertDate();
            case "PRICE_INCREASE" ->
                "Price Increase: " + alert.getSubscription().getName()
                + " price has changed";
            default ->
                "SubTrap Alert: " + alert.getSubscription().getName();
        };
    }

    // ── Build email body ──────────────────────────────────────────────────────
    private String buildEmailBody(Alert alert, Subscription sub, User user) {
        return switch (alert.getAlertType()) {
            case "RENEWAL_REMINDER" -> """
                    Hi %s,

                    This is a reminder that your subscription to %s
                    will renew on %s.

                    Amount: %s %s
                    Billing Cycle: %s

                    If you want to cancel before being charged,
                    visit: %s

                    — SubTrap Team
                    """.formatted(
                        user.getName(),
                        sub.getName(),
                        sub.getNextBillingDate(),    // ← actual renewal date
                        user.getCurrency(),
                        sub.getAmount(),
                        sub.getBillingCycle(),
                        sub.getWebsiteUrl() != null
                            ? sub.getWebsiteUrl() : "N/A"
                    );

            case "TRIAL_ENDING" -> """
                    Hi %s,

                    Your FREE TRIAL for %s ends on %s.

                    After this date you will be automatically
                    charged %s %s.

                    To cancel before being charged, visit: %s

                    — SubTrap Team
                    """.formatted(
                        user.getName(),
                        sub.getName(),
                        sub.getTrialEndDate(),       // ← actual trial end date
                        user.getCurrency(),
                        sub.getAmount(),
                        sub.getWebsiteUrl() != null
                            ? sub.getWebsiteUrl() : "N/A"
                    );

            default -> "Alert for subscription: " + sub.getName();
        };
    }
    // ── Create alert only if it doesn't already exist ─────────────────────────
    private void createAlertIfNotExists(
            Subscription sub,
            String alertType,
            LocalDate alertDate
    ) {
        boolean exists = alertRepository
                .existsBySubscriptionIdAndAlertTypeAndAlertDate(
                        sub.getId(), alertType, alertDate
                );

        if (!exists) {
            Alert alert = Alert.builder()
                    .user(sub.getUser())
                    .subscription(sub)
                    .alertType(alertType)
                    .alertDate(alertDate)
                    .isSent(false)
                    .build();
            alertRepository.save(alert);
        }
    }

    // ── Entity → DTO ──────────────────────────────────────────────────────────
    private AlertDTO toDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setSubscriptionId(alert.getSubscription().getId());
        dto.setSubscriptionName(alert.getSubscription().getName());
        dto.setAlertType(alert.getAlertType());
        dto.setAlertDate(alert.getAlertDate());
        dto.setIsSent(alert.getIsSent());
        dto.setAmount(alert.getSubscription().getAmount());
        dto.setBillingCycle(alert.getSubscription().getBillingCycle());
        return dto;
    }
    
 // Direct query — shows upcoming renewals regardless of alerts table
    public List<AlertDTO> getDirectUpcoming() {
        User user = securityUtils.getLoggedInUser();
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        return subscriptionRepository
                .findUpcomingRenewals(user.getId(), today, in7Days)
                .stream()
                .map(sub -> {
                    AlertDTO dto = new AlertDTO();
                    dto.setSubscriptionId(sub.getId());
                    dto.setSubscriptionName(sub.getName());
                    dto.setAlertType("RENEWAL_REMINDER");
                    dto.setAlertDate(sub.getNextBillingDate());
                    dto.setIsSent(false);
                    dto.setAmount(sub.getAmount());
                    dto.setBillingCycle(sub.getBillingCycle());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}