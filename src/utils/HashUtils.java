package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class HashUtils {
    public static String hash(String string) {
        return toHexString(hexHash(string));
    }

    private static byte[] hexHash(String string) {
        byte[] hex = new byte[]{0};
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hex = digest.digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ignored) {}
        return hex;
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while(hexString.length() < 64)
            hexString.insert(0, '0');

        return hexString.toString();
    }

}
