package com.example.offlinepay.service;

import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    // In production, this would be Redis: SET packet_hash "processed" NX EX 86400
    private final ConcurrentHashMap<String, Long> seenPackets = new ConcurrentHashMap<>();

    /**
     * Checks if a packet has already been processed.
     * Uses SHA-256 of the ciphertext as a unique fingerprint.
     */
    public boolean isDuplicate(String ciphertext) {
        String hash = computeHash(ciphertext);
        return seenPackets.putIfAbsent(hash, System.currentTimeMillis()) != null;
    }

    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
