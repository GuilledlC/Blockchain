package utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class HashUtils {

	public static byte[] hash(byte[] bytes) {
		byte[] hex = new byte[]{0};
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			hex = digest.digest(bytes);
		} catch (NoSuchAlgorithmException ignored) {}
		return hex;
	}

    public static byte[] hashString(String string) {
        return hash(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while(hexString.length() < 64)
            hexString.insert(0, '0');

        return hexString.toString();
    }

	public static byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
