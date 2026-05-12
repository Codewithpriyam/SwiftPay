package com.example.offlinepay.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Service
public class HybridCryptoService {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Decrypts a mesh packet using the server's RSA private key.
     * Protocol: [256 bytes RSA-encrypted AES key][12 bytes IV][AES-GCM ciphertext + 16 bytes Tag]
     */
    public String decrypt(String base64Ciphertext, String privateKeyB64) throws Exception {
        byte[] encryptedBlob = Base64.getDecoder().decode(base64Ciphertext);
        ByteBuffer bb = ByteBuffer.wrap(encryptedBlob);

        // 1. Unwrap AES Key
        byte[] rsaEncryptedKey = new byte[256];
        bb.get(rsaEncryptedKey);
        
        PrivateKey privateKey = loadPrivateKey(privateKeyB64);
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(rsaEncryptedKey);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // 2. Decrypt Payload
        byte[] iv = new byte[12];
        bb.get(iv);
        
        byte[] remaining = new byte[bb.remaining()];
        bb.get(remaining);

        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] decryptedBytes = aesCipher.doFinal(remaining);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private PrivateKey loadPrivateKey(String b64) throws Exception {
        byte[] clear = Base64.getDecoder().decode(b64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }
}
