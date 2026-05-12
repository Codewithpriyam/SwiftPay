package com.example.offlinepay.repository;

import com.example.offlinepay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByNonce(String nonce);
    List<Transaction> findByRecipientOrderByCreatedAtDesc(String recipient);
    List<Transaction> findByStatus(String status);
}
