package com.mmt.btl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class FileOrFolderUtil {

    public static byte[] calculateSHA1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1.digest(data);
    }

    // Chuyển đổi byte array thành chuỗi hex để dễ đọc
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    // Chuyển đổi một đối tượng Map thành chuỗi Bencode
    @SuppressWarnings("unchecked")
    public static byte[] encode(Object obj, ByteArrayOutputStream outputStream) throws IOException {

        // Chuyển đổi theo loại của đối tượng
        if (obj instanceof String string) {
            encodeString(string, outputStream);
        } else if (obj instanceof Integer integer) {
            encodeInt(integer, outputStream);
        } else if (obj instanceof Map) {
            encodeMap((Map<String, Object>) obj, outputStream);
        } else if (obj instanceof Object[]) {
            encodeList((Object[]) obj, outputStream);
        }

        return outputStream.toByteArray();
    }

    // Mã hóa chuỗi
    private static void encodeString(String value, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write((value.length() + ":").getBytes());
        outputStream.write(value.getBytes());
    }

    // Mã hóa số nguyên
    private static void encodeInt(Integer value, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write(("i" + value + "e").getBytes());
    }

    // Mã hóa Map
    private static void encodeMap(Map<String, Object> map, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write("d".getBytes());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            encodeString(entry.getKey(), outputStream);
            encode(entry.getValue(), outputStream);
        }
        outputStream.write("e".getBytes());
    }

    // Mã hóa danh sách
    private static void encodeList(Object[] list, ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write("l".getBytes());
        for (Object obj : list) {
            encode(obj, outputStream);
        }
        outputStream.write("e".getBytes());
    }

}