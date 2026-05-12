package com.example.offlinepay.dto;

import lombok.Data;

/** Incoming SMS payload from Twilio webhook or direct POST */
@Data
public class SmsPayloadRequest {
    private String encryptedPayload; // Base64 RSA-encrypted bundle
    private String fromNumber;       // Sender's phone number (for reply SMS)
}
