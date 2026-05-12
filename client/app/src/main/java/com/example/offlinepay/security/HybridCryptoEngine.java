package com.example.offlinepay.security;

import android.util.Base64;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class HybridCryptoEngine {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Encrypts the payload using Hybrid Encryption:
     * 1. Generate a random AES-256 key.
     * 2. Encrypt data with AES-GCM.
     * 3. Wrap AES key with the server's RSA Public Key.
     * Result: [256 bytes RSA Key][12 bytes IV][Ciphertext + Tag]
     */
    public static String encrypt(String cleartext, String serverPubKeyB64) throws Exception {
        // 1. Prepare Server Public Key
        byte[] publicKeyBytes = Base64.decode(serverPubKeyB64, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

        // 2. Generate ephemeral AES key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();

        // 3. Encrypt data with AES-GCM
        Cipher aesCipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] ciphertext = aesCipher.doFinal(cleartext.getBytes());

        // 4. Wrap AES key with RSA
        Cipher rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] wrappedKey = rsaCipher.doFinal(aesKey.getEncoded());

        // 5. Concatenate
        ByteBuffer bb = ByteBuffer.allocate(wrappedKey.length + iv.length + ciphertext.length);
        bb.put(wrappedKey);
        bb.put(iv);
        bb.put(ciphertext);

        return Base64.encodeToString(bb.array(), Base64.NO_WRAP);
    }
}
