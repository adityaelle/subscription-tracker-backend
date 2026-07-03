package com.aurionpro.util;

import com.aurionpro.DTO.SubscriptionDTO;
import com.aurionpro.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

    // Entity → DTO (for sending to frontend)
    public SubscriptionDTO toDTO(Subscription sub) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(sub.getId());
        dto.setName(sub.getName());
        dto.setCategory(sub.getCategory());
        dto.setAmount(sub.getAmount());
        dto.setBillingCycle(sub.getBillingCycle());
        dto.setNextBillingDate(sub.getNextBillingDate());
        dto.setStatus(sub.getStatus());
        dto.setIsTrial(sub.getIsTrial());
        dto.setTrialEndDate(sub.getTrialEndDate());
        dto.setWebsiteUrl(sub.getWebsiteUrl());
        dto.setNotes(sub.getNotes());
        dto.setLastUsedDate(sub.getLastUsedDate());
        dto.setCreatedAt(sub.getCreatedAt());
        return dto;
    }

    // DTO → Entity (for saving from frontend request)
    public Subscription toEntity(SubscriptionDTO dto) {
        Subscription sub = new Subscription();
        sub.setName(dto.getName());
        sub.setCategory(dto.getCategory());
        sub.setAmount(dto.getAmount());
        sub.setBillingCycle(dto.getBillingCycle());
        sub.setNextBillingDate(dto.getNextBillingDate());
        sub.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        sub.setIsTrial(dto.getIsTrial() != null ? dto.getIsTrial() : false);
        sub.setTrialEndDate(dto.getTrialEndDate());
        sub.setWebsiteUrl(dto.getWebsiteUrl());
        sub.setNotes(dto.getNotes());
        sub.setLastUsedDate(dto.getLastUsedDate());
        return sub;
    }

    // Update existing entity from DTO (for PUT requests)
    public void updateEntityFromDTO(SubscriptionDTO dto, Subscription sub) {
        sub.setName(dto.getName());
        sub.setCategory(dto.getCategory());
        sub.setBillingCycle(dto.getBillingCycle());
        sub.setNextBillingDate(dto.getNextBillingDate());
        sub.setStatus(dto.getStatus());
        sub.setIsTrial(dto.getIsTrial() != null ? dto.getIsTrial() : false);
        sub.setTrialEndDate(dto.getTrialEndDate());
        sub.setWebsiteUrl(dto.getWebsiteUrl());
        sub.setNotes(dto.getNotes());
        sub.setLastUsedDate(dto.getLastUsedDate());
        // NOTE: amount is NOT set here — handled separately in service
        // because we need to detect price changes first
    }
}