package com.example.offlinepay.service;

import com.example.offlinepay.model.Transaction;
import com.example.offlinepay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * PaymentService — orchestrates the full payment processing pipeline:
 * 1. Decrypt the incoming encrypted payload
 * 2. Verify the device RSA signature
 * 3. Check for replay attacks (nonce uniqueness)
 * 4. Persist the transaction to PostgreSQL
 * 5. Trigger the success SMS via SmsNotificationService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CryptoService           cryptoService;
    private final TransactionRepository   transactionRepository;
    private final SmsNotificationService  smsNotificationService;

    @Transactional
    @Transactional
    public Transaction processPayment(String rawPayload, String fromNumber) {
        log.info("Processing incoming payment from {}", fromNumber);

        try {
            // Step 1: Parse, Decrypt, and Verify
            CryptoService.ParsedPayment parsed = cryptoService.parseIncomingMessage(rawPayload);

            // Step 2: Verify signature
            boolean signatureValid = cryptoService.verifySignature(
                    parsed.paymentString, parsed.signatureBase64, parsed.devicePublicKey);

            if (!signatureValid) {
                log.warn("Signature verification FAILED for nonce {}", parsed.nonce);
                return saveFailedTransaction(parsed, rawPayload, "SIGNATURE_INVALID");
            }

            // Step 3: Replay attack check
            if (transactionRepository.existsByNonce(parsed.nonce)) {
                log.warn("Replay attack detected! Nonce {} already used.", parsed.nonce);
                return saveFailedTransaction(parsed, rawPayload, "REPLAY_ATTACK");
            }

            // Step 4: Persist success transaction
            Transaction tx = Transaction.builder()
                    .recipient(parsed.recipient)
                    .amount(new BigDecimal(parsed.amount))
                    .nonce(parsed.nonce)
                    .status("SUCCESS")
                    .devicePublicKey(parsed.devicePublicKey)
                    .rawPayload(rawPayload)
                    .build();

            Transaction saved = transactionRepository.save(tx);
            log.info("Transaction {} saved. Sending success SMS.", saved.getId());

            // Step 5: Send success SMS back to sender
            smsNotificationService.sendSuccessSms(fromNumber, parsed.recipient, parsed.amount);

            return saved;

        } catch (Exception e) {
            log.error("Payment processing failed", e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Transaction processMeshPayment(String cleartext, String ciphertextHash) {
        log.info("Processing mesh payment cleartext");

        try {
            // Step 1: Parse cleartext (Format: REC=...;AMT=...;NONCE=...;TS=...)
            Map<String, String> params = new HashMap<>();
            for (String part : cleartext.split(";")) {
                String[] kv = part.split("=");
                if (kv.length == 2) params.put(kv[0], kv[1]);
            }

            String recipient = params.get("REC");
            String amount    = params.get("AMT");
            String nonce     = params.get("NONCE");

            // Step 2: Persist
            Transaction tx = Transaction.builder()
                    .recipient(recipient)
                    .amount(new BigDecimal(amount))
                    .nonce(nonce)
                    .packetHash(ciphertextHash)
                    .status("SUCCESS")
                    .rawPayload(cleartext)
                    .build();

            Transaction saved = transactionRepository.save(tx);
            log.info("Mesh transaction {} settled successfully", saved.getId());
            return saved;

        } catch (Exception e) {
            log.error("Mesh processing failed", e);
            throw new RuntimeException("Mesh processing failed: " + e.getMessage(), e);
        }
    }

    private Transaction saveFailedTransaction(CryptoService.ParsedPayment parsed,
                                               String rawPayload, String reason) {
        Transaction tx = Transaction.builder()
                .recipient(parsed.recipient)
                .amount(new BigDecimal(parsed.amount))
                .nonce(parsed.nonce + "_FAILED_" + System.currentTimeMillis())
                .status(reason)
                .devicePublicKey(parsed.devicePublicKey)
                .rawPayload(rawPayload)
                .packetHash("FAILED_" + System.currentTimeMillis())
                .build();
        return transactionRepository.save(tx);
    }
}
