package com.aurionpro.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.entity.ParsedTransaction;

public interface ParsedTransactionRepository extends JpaRepository<ParsedTransaction, Long> {
    List<ParsedTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);
}