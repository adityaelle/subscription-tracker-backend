package com.aurionpro.Repository;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.entity.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserIdOrderByAlertDateAsc(Long userId);

    // Used by scheduler to find unsent alerts due today
    List<Alert> findByAlertDateAndIsSentFalse(LocalDate alertDate);

    // Check if alert already exists (avoid duplicates)
    boolean existsBySubscriptionIdAndAlertTypeAndAlertDate(
        Long subscriptionId, String alertType, LocalDate alertDate
    );

    List<Alert> findByUserIdAndIsSentFalseOrderByAlertDateAsc(Long userId);
}