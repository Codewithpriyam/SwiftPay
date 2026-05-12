package com.example.offlinepay.security;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class RsaCryptoEngine {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    /**
     * Encrypts and Signs the payment data for offline transmission.
     * Format: SWIFT:[EncryptedData]|SIG:[Signature]|PUB:[DevicePublicKey]
     */
    public static String buildSecurePayload(String recipient, String amount, String nonce, String serverPubKeyB64) throws Exception {
        // 1. Prepare data string
        String data = "REC=" + recipient + ";AMT=" + amount + ";NONCE=" + nonce + ";TS=" + System.currentTimeMillis();
        
        // 2. Generate ephemeral device keypair for this session/device
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair deviceKeyPair = kpg.generateKeyPair();
        
        // 3. Encrypt data with Server's Public Key
        byte[] serverPubKeyBytes = Base64.decode(serverPubKeyB64, Base64.DEFAULT);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(serverPubKeyBytes);
        PublicKey serverKey = KeyFactory.getInstance("RSA").generatePublic(spec);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, serverKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        String encryptedB64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        
        // 4. Sign the encrypted data with Device's Private Key
        Signature sig = Signature.getInstance(SIGN_ALGORITHM);
        sig.initSign(deviceKeyPair.getPrivate());
        sig.update(encryptedBytes);
        String signatureB64 = Base64.encodeToString(sig.sign(), Base64.NO_WRAP);
        
        // 5. Get Device's Public Key (to let server verify the signature)
        String devicePubKeyB64 = Base64.encodeToString(deviceKeyPair.getPublic().getEncoded(), Base64.NO_WRAP);
        
        return "SWIFT:" + encryptedB64 + "|SIG:" + signatureB64 + "|PUB:" + devicePubKeyB64;
    }

    public static String generateNonce() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
