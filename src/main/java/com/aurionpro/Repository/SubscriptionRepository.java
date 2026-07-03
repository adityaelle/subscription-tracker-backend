package com.aurionpro.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aurionpro.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // All subscriptions for a user
    List<Subscription> findByUserId(Long userId);

    // Subscriptions by user + status
    List<Subscription> findByUserIdAndStatus(Long userId, String status);

    // For the /upcoming endpoint — user's subscriptions due between two dates
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
           "AND s.nextBillingDate BETWEEN :from AND :to " +
           "AND s.status = 'ACTIVE'")
    List<Subscription> findUpcomingRenewals(
        @Param("userId") Long userId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    // For the scheduler — all users, subscriptions renewing on a specific date
    @Query("SELECT s FROM Subscription s WHERE " +
           "s.nextBillingDate = :targetDate AND s.status = 'ACTIVE'")
    List<Subscription> findAllUpcomingRenewals(
        @Param("targetDate") LocalDate targetDate
    );

    // For the scheduler — trials ending on a specific date
    @Query("SELECT s FROM Subscription s WHERE " +
           "s.isTrial = true AND s.trialEndDate = :targetDate " +
           "AND s.status = 'ACTIVE'")
    List<Subscription> findTrialsEndingSoon(
        @Param("targetDate") LocalDate targetDate
    );
}