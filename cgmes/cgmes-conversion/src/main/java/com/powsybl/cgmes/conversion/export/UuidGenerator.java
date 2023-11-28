package com.powsybl.cgmes.conversion.export;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public final class UuidGenerator {

    private UuidGenerator() { }

    public static UUID generateType5Uuid(String namespace, String name) {

        final byte[] nameSpaceBytes = bytesFromUUID(namespace);
        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        final byte[] result = joinBytes(nameSpaceBytes, nameBytes);

        return type5UuidFromBytes(result);
    }

    public static UUID type5UuidFromBytes(byte[] name) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException exception) {
            throw new InternalError("SHA-1 not supported", exception);
        }
        final byte[] bytes = Arrays.copyOfRange(md.digest(name), 0, 16);
        bytes[6] &= 0x0f; /* clear version        */
        bytes[6] |= 0x50; /* set to version 5     */
        bytes[8] &= 0x3f; /* clear variant        */
        bytes[8] |= 0x80; /* set to IETF variant  */
        return constructType5UUID(bytes);
    }

    private static UUID constructType5UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";

        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }

        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    private static byte[] bytesFromUUID(String uuidHexString) {
        final String normalizedUUIDHexString = uuidHexString.replace("-", "");

        assert normalizedUUIDHexString.length() == 32;

        final byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            final byte b = hexToByte(normalizedUUIDHexString.substring(i * 2, i * 2 + 2));
            bytes[i] = b;
        }
        return bytes;
    }

    public static byte hexToByte(String hexString) {
        final int firstDigit = Character.digit(hexString.charAt(0), 16);
        final int secondDigit = Character.digit(hexString.charAt(1), 16);
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    public static byte[] joinBytes(byte[] byteArray1, byte[] byteArray2) {
        final int finalLength = byteArray1.length + byteArray2.length;
        final byte[] result = new byte[finalLength];

        System.arraycopy(byteArray1, 0, result, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, result, byteArray1.length, byteArray2.length);
        return result;
    }
}
