package com.aurionproscheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aurionpro.Service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final AlertService alertService;

    // Runs every day at 9:00 AM
    // Cron format: second minute hour day month weekday
    @Scheduled(cron = "0 0 9 * * *")
    public void runDailyAlertJob() {
        log.info("=== Daily Alert Job Started ===");

        // Step 1: Generate alerts for upcoming renewals and trials
        alertService.generateAlertsForAllUsers();

        // Step 2: Send emails for all alerts due today
        alertService.sendDueAlerts();

        log.info("=== Daily Alert Job Completed ===");
    }

    // This second method runs every minute — ONLY FOR TESTING
    // Comment this out in production
    // @Scheduled(cron = "0 * * * * *")
    // public void runEveryMinuteForTesting() {
    //     log.info("=== Test Alert Job Running ===");
    //     alertService.generateAlertsForAllUsers();
    //     alertService.sendDueAlerts();
    // }
}