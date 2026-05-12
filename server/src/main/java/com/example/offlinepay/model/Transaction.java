package com.example.offlinepay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;       // VPA of the payee

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(unique = true, nullable = true)
    private String packetHash;

    @Column(nullable = false, unique = true)
    private String nonce;           // Replay-attack prevention

    @Column(nullable = false)
    private String status;          // PENDING | SUCCESS | FAILED

    @Column(name = "device_pub_key", columnDefinition = "TEXT")
    private String devicePublicKey; // Base64 RSA public key of the sender

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;      // Original encrypted SMS (for audit)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
