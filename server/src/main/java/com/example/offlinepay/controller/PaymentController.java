package com.example.offlinepay.controller;

import com.example.offlinepay.dto.MeshPacket;
import com.example.offlinepay.model.Transaction;
import com.example.offlinepay.service.CryptoService;
import com.example.offlinepay.service.HybridCryptoService;
import com.example.offlinepay.service.IdempotencyService;
import com.example.offlinepay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService       paymentService;
    private final CryptoService        cryptoService;
    private final HybridCryptoService  hybridCryptoService;
    private final IdempotencyService   idempotencyService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("service", "SwiftPay Mesh Backend");
        response.put("version", "2.0.0 (Mesh-Enabled)");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", cryptoService.getServerPublicKeyBase64());
        return ResponseEntity.ok(response);
    }

    /**
     * /api/bridge/ingest — Upload a mesh packet for settlement.
     * Called by any device in the mesh that gets internet access.
     */
    @PostMapping("/bridge/ingest")
    public ResponseEntity<Map<String, Object>> ingestMeshPacket(
            @RequestBody MeshPacket packet,
            @RequestHeader(value = "X-Bridge-Node-Id", required = false) String bridgeId) {

        log.info("Ingesting mesh packet {} from bridge {}", packet.getPacketId(), bridgeId);
        Map<String, Object> response = new HashMap<>();

        // 1. Idempotency Check (SHA-256 fingerprint)
        if (idempotencyService.isDuplicate(packet.getCiphertext())) {
            log.warn("Duplicate packet {} dropped", packet.getPacketId());
            response.put("status", "DUPLICATE_DROPPED");
            return ResponseEntity.ok(response);
        }

        try {
            // 2. Hybrid Decryption (RSA-OAEP + AES-GCM)
            String cleartext = hybridCryptoService.decrypt(
                    packet.getCiphertext(), 
                    cryptoService.getServerPrivateKeyBase64()
            );

            // 3. Settlement (Process the decrypted JSON)
            Transaction tx = paymentService.processMeshPayment(cleartext, packet.getCiphertext());

            response.put("status", "SUCCESS");
            response.put("transactionId", tx.getId());
            response.put("recipient", tx.getRecipient());
            response.put("amount", tx.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Mesh ingestion failed", e);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
