package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.DTO.PriceHistoryDTO;
import com.aurionpro.DTO.SubscriptionDTO;
import com.aurionpro.Service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // GET /api/subscriptions
    // Returns all subscriptions for the logged-in user
    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAllForCurrentUser());
    }

    // GET /api/subscriptions/active
    // Returns only ACTIVE subscriptions
    @GetMapping("/active")
    public ResponseEntity<List<SubscriptionDTO>> getActive() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }

    // GET /api/subscriptions/upcoming
    // Returns subscriptions renewing in next 7 days
    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionDTO>> getUpcoming() {
        return ResponseEntity.ok(subscriptionService.getUpcomingRenewals());
    }

    // GET /api/subscriptions/{id}
    // Returns single subscription by ID
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getById(id));
    }

    // POST /api/subscriptions
    // Creates a new subscription
    @PostMapping
    public ResponseEntity<SubscriptionDTO> create(
            @Valid @RequestBody SubscriptionDTO dto
    ) {
        SubscriptionDTO created = subscriptionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/subscriptions/{id}
    // Updates subscription — auto-logs price change if amount changed
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionDTO dto
    ) {
        return ResponseEntity.ok(subscriptionService.update(id, dto));
    }

    // PATCH /api/subscriptions/{id}/cancel
    // Soft cancel — sets status to CANCELLED, keeps data
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        subscriptionService.cancel(id);
        return ResponseEntity.ok("Subscription cancelled successfully");
    }

    // DELETE /api/subscriptions/{id}
    // Hard delete — removes from DB permanently
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/subscriptions/{id}/price-history
    // Returns all price changes for a subscription
    @GetMapping("/{id}/price-history")
    public ResponseEntity<List<PriceHistoryDTO>> getPriceHistory(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(subscriptionService.getPriceHistory(id));
    }
}