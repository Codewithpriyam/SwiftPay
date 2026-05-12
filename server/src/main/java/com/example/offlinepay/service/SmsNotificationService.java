package com.example.offlinepay.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * SmsNotificationService — sends transactional SMS via Twilio.
 * Configure Twilio credentials in application.properties.
 */
@Slf4j
@Service
public class SmsNotificationService {

    @Value("${twilio.account-sid:ACplaceholder}")
    private String accountSid;

    @Value("${twilio.auth-token:placeholder_token}")
    private String authToken;

    @Value("${twilio.from-number:+15005550006}")
    private String fromNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized with account: {}", accountSid);
    }

    public void sendSuccessSms(String toNumber, String recipient, String amount) {
        if (toNumber == null || toNumber.isBlank()) {
            log.warn("Cannot send success SMS — toNumber is blank.");
            return;
        }

        String body = String.format(
                "OfflinePay: ✅ Payment of ₹%s to %s was SUCCESSFUL. Your funds are on the way.",
                amount, recipient);

        try {
            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("Success SMS sent. SID: {}", message.getSid());
        } catch (Exception e) {
            log.error("Failed to send success SMS to {}", toNumber, e);
        }
    }

    public void sendFailureSms(String toNumber, String reason) {
        if (toNumber == null || toNumber.isBlank()) return;

        String body = "OfflinePay: ❌ Your payment failed. Reason: " + reason +
                ". Please retry or contact support.";

        try {
            Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
        } catch (Exception e) {
            log.error("Failed to send failure SMS to {}", toNumber, e);
        }
    }
}
