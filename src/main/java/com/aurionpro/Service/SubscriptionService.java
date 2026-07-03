package com.aurionpro.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.DTO.PriceHistoryDTO;
import com.aurionpro.DTO.SubscriptionDTO;
import com.aurionpro.Repository.PriceHistoryRepository;
import com.aurionpro.Repository.SubscriptionRepository;
import com.aurionpro.entity.PriceHistory;
import com.aurionpro.entity.Subscription;
import com.aurionpro.entity.User;
import com.aurionpro.util.SecurityUtils;
import com.aurionpro.util.SubscriptionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SecurityUtils securityUtils;
    private final SubscriptionMapper mapper;

    // ── Get all subscriptions for logged-in user ──────────────────────────────
    public List<SubscriptionDTO> getAllForCurrentUser() {
        User user = securityUtils.getLoggedInUser();
        return subscriptionRepository
                .findByUserId(user.getId())
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get single subscription by ID ─────────────────────────────────────────
    public SubscriptionDTO getById(Long id) {
        Subscription sub = findAndVerifyOwnership(id);
        return mapper.toDTO(sub);
    }

    // ── Create new subscription ───────────────────────────────────────────────
    @Transactional
    public SubscriptionDTO create(SubscriptionDTO dto) {
        User user = securityUtils.getLoggedInUser();

        Subscription sub = mapper.toEntity(dto);
        sub.setUser(user);

        Subscription saved = subscriptionRepository.save(sub);
        return mapper.toDTO(saved);
    }

    // ── Update subscription ───────────────────────────────────────────────────
    // KEY LOGIC: if amount changed, auto-log into price_history
    @Transactional
    public SubscriptionDTO update(Long id, SubscriptionDTO dto) {
        Subscription existing = findAndVerifyOwnership(id);

        // ── Price change detection ─────────────────────────────────────────
        BigDecimal oldAmount = existing.getAmount();
        BigDecimal newAmount = dto.getAmount();

        boolean amountChanged = oldAmount.compareTo(newAmount) != 0;

        if (amountChanged) {
            // Auto-log the price change into price_history table
            PriceHistory history = PriceHistory.builder()
                    .subscription(existing)
                    .oldAmount(oldAmount)
                    .newAmount(newAmount)
                    .build();
            priceHistoryRepository.save(history);
        }

        // Update all other fields
        mapper.updateEntityFromDTO(dto, existing);

        // Now update amount after logging the change
        existing.setAmount(newAmount);

        Subscription saved = subscriptionRepository.save(existing);
        return mapper.toDTO(saved);
    }

    // ── Cancel subscription (soft delete — status = CANCELLED) ───────────────
    @Transactional
    public void cancel(Long id) {
        Subscription sub = findAndVerifyOwnership(id);
        sub.setStatus("CANCELLED");
        subscriptionRepository.save(sub);
    }

    // ── Hard delete ───────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        Subscription sub = findAndVerifyOwnership(id);
        subscriptionRepository.delete(sub);
    }

    // ── Get price history for a subscription ──────────────────────────────────
    public List<PriceHistoryDTO> getPriceHistory(Long id) {
        // Verify ownership first
        findAndVerifyOwnership(id);

        return priceHistoryRepository
                .findBySubscriptionIdOrderByChangedAtDesc(id)
                .stream()
                .map(this::toPriceHistoryDTO)
                .collect(Collectors.toList());
    }

    // ── Get upcoming renewals (next 7 days) ───────────────────────────────────
    public List<SubscriptionDTO> getUpcomingRenewals() {
        User user = securityUtils.getLoggedInUser();
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        return subscriptionRepository
                .findUpcomingRenewals(user.getId(), today, in7Days)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ── Get active subscriptions only ─────────────────────────────────────────
    public List<SubscriptionDTO> getActiveSubscriptions() {
        User user = securityUtils.getLoggedInUser();
        return subscriptionRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE")
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ── Private helper: find subscription and verify it belongs to this user ──
    private Subscription findAndVerifyOwnership(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Subscription not found with id: " + id)
                );

        User loggedInUser = securityUtils.getLoggedInUser();

        // Security check — make sure this subscription belongs to the logged-in user
        if (!sub.getUser().getId().equals(loggedInUser.getId())) {
            throw new RuntimeException("Access denied: subscription does not belong to you");
        }

        return sub;
    }

    // ── Private helper: PriceHistory entity → DTO ────────────────────────────
    private PriceHistoryDTO toPriceHistoryDTO(PriceHistory ph) {
        PriceHistoryDTO dto = new PriceHistoryDTO();
        dto.setId(ph.getId());
        dto.setSubscriptionId(ph.getSubscription().getId());
        dto.setOldAmount(ph.getOldAmount());
        dto.setNewAmount(ph.getNewAmount());
        dto.setChangedAt(ph.getChangedAt());
        return dto;
    }
}