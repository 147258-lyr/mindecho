package com.mindecho.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class Encryptor {
    private static final String KEY_PATH;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    
    private SecretKey secretKey;

    static {
        String appData = System.getenv("APPDATA");
        KEY_PATH = appData + "/MindEcho/key.bin";
    }

    public Encryptor() {
        loadOrGenerateKey();
    }

    private void loadOrGenerateKey() {
        Path keyPath = Paths.get(KEY_PATH);
        
        if (Files.exists(keyPath)) {
            try {
                byte[] keyBytes = Files.readAllBytes(keyPath);
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            } catch (IOException e) {
                e.printStackTrace();
                generateAndSaveKey();
            }
        } else {
            generateAndSaveKey();
        }
    }

    private void generateAndSaveKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            secretKey = keyGen.generateKey();
            
            Path keyDir = Paths.get(System.getenv("APPDATA") + "/MindEcho");
            if (!Files.exists(keyDir)) {
                Files.createDirectories(keyDir);
            }
            
            Files.write(Paths.get(KEY_PATH), secretKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
        
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        
        return result;
    }

    public String decrypt(byte[] cipherData) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(cipherData, 0, iv, 0, iv.length);
        
        byte[] encrypted = new byte[cipherData.length - GCM_IV_LENGTH];
        System.arraycopy(cipherData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }
}
