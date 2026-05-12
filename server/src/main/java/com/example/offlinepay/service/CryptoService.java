package com.example.offlinepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
public class CryptoService {

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    // Static keys for demonstration - In production, use a secure vault.
    private static final String PRIVATE_KEY_B64 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDhPhOv17oFG25qxT/N8OSp0RDUdxA/bYo1mqVyFm3n+ml3u9ykkOwbUTVkiW6h07tXEu3qS2XFxPZpQuFvcRgLWNA5KXwK73nNvn8XgUTxKWNjosrGsbCoI6JE+NM3tcR1BDXVlY71Fnxlal0/e20wa1U84uqWnrx5RZvX9h+pQcR8v2VX5NjhgDnn5g/y1Bx2EKFk5uqq0OcL9yhP59f3V+Htmx7IscyBH2GpGcn0iejPlitKEkivWV8fhd7zIGr0/zBFEAmH3HkpsqiMs61JCpflykP9eNofXR98ogUDX66MfVT8dohpoYuu47JCT5cyN9nSz3R6iwAYZyc9SW1vAgMBAAECggEACfnh4qvqBUfjFWo0b+lMzL4BwidJL5tTv8QLB96OckkuWqtAvoPcsT5g4Fm7Q1eeuIs8wQp4hiXkdA0ORw1PRKa5B48a0LEtR+Y5Ys/K6ejXX2uvvNUp6d3fgLE0nwcMndIt6uPQ4nIN2Gq5mTzc59lue7U2hq1JKRCNh9HS3SrYmXj+EFxOhRdbaxVxus0ViUBA+EXBRpi2F66PuoOMNBUZMMee3YiYXNLF5x1sSQShRmEl6Dj5qxr9+1gDyTvLqJGh5+G5nsxF7jdLELmcyNQ1ilciSXyt9v23kqhZxgEojgX8Yueq7giQurs1TXRDBpHEqzrgu0PaIFX3lXX2IQKBgQDsD116M24/9v674X9GRNd0P41OjEZ/VnIhvM/VypurhWuIB7eOom+LkV/pE0Tw0ozPYN8jDTcQCy6CHzgPquBP3aDbfDpSsHGXrH3esZwaaLonEP6GpFy6QNhjkrHZ8Z7oItCmQBA4WHRDjCHiPSq1fA3jswFDPgST5Lxn7FPn/QKBgQD0RMpDsDCcUg6tMyDdphhhGTEqWQh4WfT+bLq36Jwh5O6XO8cjCfdqNENnuHaXaw0B4jZ6TLZdjvFDsGZYhVFuUKPYeQWPSLpPOBI4gl3/FOiDxiekXRThzyRRGzdkPzz2doDX0J7yyIyGDT45Fv9x3bkFjviHenH01A489L9Y2wKBgQCx+aetkYcm/M7z0kDGNvMGN0APn50rNn7YRuWft6EEgZQgPEk9ZeSZoqJgNOH+e5AkblHtuHHyS6vl+SmoTlnOfTdhI9lJLLSH+UnxE5GxK5JmD98Gnnc3Cdrbv+cNfakkNdN/9L8F7FLD9qw5SYqgyAYkzXo/O0vyQTt3Uuuz9QKBgGX0GGAsN/3nGg0cnAknfGF6vFSqmKhzE5jmFQER66kR/qvM3Y84Z7ZCXif6jDUjD2jL3GUrg6qFMRpJlE46RwS+T8TYroNhnbHW+3PupgED3xCxMnaeC0s8xDG+JF6JLo7IY3qwrsczAcQp3uDBQplqLAGv7PfPGaVQpSi4Fu2lAoGAMy8zg/K2NtC4sy4OO2LWYCMW63lj6JnrN2sHwrj+z0iO3ffLZ6jLTPpI3lgQWQRYK/R+qOv3orhN6q/TS+9CmNF+86nUIgaz/R8pBFqFUBHK4i5jk89DJ3Dpq6piBuAcaYTCt8SvqmEA2mKmqLHxylVpVBwQM4ptHqT8hFn4zFE=";
    private static final String PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4T4Tr9e6BRtuasU/zfDkqdEQ1HcQP22KNZqlchZt5/ppd7vcpJDsG1E1ZIluodO7VxLt6ktlxcT2aULhb3EYC1jQOSl8Cu95zb5/F4FE8SljY6LKxrGwqCOiRPjTN7XEdQQ11ZWO9RZ8ZWpdP3ttMGtVPOLqlp68eUWb1/YfqUHEfL9lV+TY4YA55+YP8tQcdhChZObqqtDnC/coT+fX91fh7ZseyLHMgR9hqRnJ9Inoz5YrShJIr1lfH4Xe8yBq9P8wRRAJh9x5KbKojLOtSQqX5cpD/XjaH10ffKIFA1+ujH1U/HaIaaGLruOyQk+XMjfZ0s90eosAGGcnPUltbwIDAQAB";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public CryptoService() throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(PRIVATE_KEY_B64);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(privateKeySpec);

        byte[] publicKeyBytes = Base64.getDecoder().decode(PUBLIC_KEY_B64);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
        
        log.info("Server RSA keys initialized.");
    }

    public String getServerPublicKeyBase64() {
        return PUBLIC_KEY_B64;
    }

    public String decryptPayload(String encryptedBase64) throws Exception {
        byte[] cipherBytes = Base64.getDecoder().decode(encryptedBase64);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes, "UTF-8");
    }

    public boolean verifySignature(String data, String signatureBase64, String devicePublicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(devicePublicKeyBase64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKey = kf.generatePublic(spec);

            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(pubKey);
            sig.update(data.getBytes("UTF-8"));
            return sig.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    public ParsedPayment parseIncomingMessage(String rawMessage) throws Exception {
        // Expected format: OFFPAY:SWIFT:[EncryptedContent]|SIG:[Signature]|PUB:[DevicePublicKey]
        // Note: The device public key needs to be sent if the server doesn't know it.
        // For now, let's assume the payload includes the public key at the end.
        
        String work = rawMessage;
        if (work.startsWith("OFFPAY:")) {
            work = work.substring(7);
        }
        
        if (!work.startsWith("SWIFT:")) {
            throw new IllegalArgumentException("Invalid message format: missing SWIFT prefix");
        }
        work = work.substring(6);
        
        String[] parts = work.split("\\|SIG:");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid message format: missing signature");
        }
        
        String encryptedPart = parts[0];
        String remainder = parts[1];
        
        String[] subParts = remainder.split("\\|PUB:");
        if (subParts.length < 2) {
            throw new IllegalArgumentException("Invalid message format: missing public key");
        }
        
        String signature = subParts[0];
        String devicePublicKey = subParts[1];
        
        String decryptedData = decryptPayload(encryptedPart);
        // decryptedData is expected to be "vpa|amount|nonce"
        String[] dataParts = decryptedData.split("\\|");
        
        if (dataParts.length < 3) {
            throw new IllegalArgumentException("Decrypted data format invalid");
        }
        
        ParsedPayment p = new ParsedPayment();
        p.recipient = dataParts[0];
        p.amount = dataParts[1];
        p.nonce = dataParts[2];
        p.paymentString = decryptedData;
        p.signatureBase64 = signature;
        p.devicePublicKey = devicePublicKey;
        
        return p;
    }

    public static class ParsedPayment {
        public String recipient;
        public String amount;
        public String nonce;
        public String paymentString;
        public String signatureBase64;
        public String devicePublicKey;
    }
}

