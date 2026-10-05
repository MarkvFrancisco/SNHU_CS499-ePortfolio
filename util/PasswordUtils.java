package com.example.cs360_projectthree.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


    // Utility class providing secure password hashing and verification using PBKDF2 with HMAC-SHA512.

public class PasswordUtils {

    // PBKDF2 parameters
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 210000; // OWASP recommended iteration count for PBKDF2-HMAC-SHA512
    private static final int KEY_LENGTH_BITS = 512; // 512 bits (64 bytes) matching SHA-512 output size
    private static final int SALT_LENGTH_BYTES = 16; // 128-bit salt


    //Generates a cryptographically secure random salt byte array.

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return salt;
    }


     // Hashes a password using PBKDF2-HMAC-SHA512 with the given salt.
    public static byte[] hashPassword(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with " + ALGORITHM, e);
        }
    }


    // Hashes a password string using PBKDF2-HMAC-SHA512 and returns the string as hex
    public static String hashPasswordHex(String password, byte[] salt) {
        byte[] hash = hashPassword(password.toCharArray(), salt);
        return bytesToHex(hash);
    }


     //     Verifies the raw password against a stored hex-encoded hash and stored hex-encoded salt.
     //     Uses MessageDigest.isEqual to prevent timing attack vulnerabilities.

    public static boolean verifyPassword(String password, String storedHashHex, String storedSaltHex) {
        if (password == null || storedHashHex == null || storedSaltHex == null) {
            return false;
        }

        try {
            byte[] salt = hexToBytes(storedSaltHex);
            byte[] storedHash = hexToBytes(storedHashHex);
            byte[] computedHash = hashPassword(password.toCharArray(), salt);

            return MessageDigest.isEqual(storedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }


    //      Converts a byte array into a hexadecimal string

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


     // Converts a hexadecimal string back into a byte array

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
