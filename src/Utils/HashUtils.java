package Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class HashUtils {
    public static String Hash(String string) {
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

    public static byte[] Sign(String text, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);

        byte[] bytes = text.getBytes();
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean Verify(String text, byte[] signed, PublicKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);

        byte[] bytes = text.getBytes();
        signature.update(bytes);
        return signature.verify(signed);
    }
}
