package com.octskyout.users.aes;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Aes256 {
    private final String algorithm;

    private final String key;

    private final String iv;

    public Aes256(String algorithm, String key, String iv) {
        this.algorithm = algorithm;
        this.key = key;
        this.iv = iv;
    }

    @SuppressWarnings("java:S3329")
    public String encrypt(String str)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
        IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedBase64)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
        IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedStr = Base64.getDecoder().decode(encryptedBase64);
        byte[] decrypted = cipher.doFinal(decryptedStr);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
