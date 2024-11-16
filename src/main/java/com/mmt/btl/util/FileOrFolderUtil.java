// package com.mmt.btl.util;

// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.Map;

// public class FileOrFolderUtil {

//     public static byte[] calculateSHA1(byte[] data) throws NoSuchAlgorithmException {
//         MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
//         return sha1.digest(data);
//     }

//     // Chuyển đổi byte array thành chuỗi hex để dễ đọc
//     public static String bytesToHex(byte[] bytes) {
//         StringBuilder hexString = new StringBuilder();
//         for (byte b : bytes) {
//             hexString.append(String.format("%02x", b));
//         }
//         return hexString.toString();
//     }

//     // Chuyển đổi một đối tượng Map thành chuỗi Bencode
//     @SuppressWarnings("unchecked")
//     public static byte[] encode(Object obj, ByteArrayOutputStream outputStream) throws IOException {

//         // Chuyển đổi theo loại của đối tượng
//         if (obj instanceof String string) {
//             encodeString(string, outputStream);
//         } else if (obj instanceof Integer integer) {
//             encodeInt(integer, outputStream);
//         } else if (obj instanceof Map) {
//             encodeMap((Map<String, Object>) obj, outputStream);
//         } else if (obj instanceof Object[]) {
//             encodeList((Object[]) obj, outputStream);
//         }

//         return outputStream.toByteArray();
//     }

//     // Mã hóa chuỗi
//     private static void encodeString(String value, ByteArrayOutputStream outputStream) throws IOException {
//         outputStream.write((value.length() + ":").getBytes());
//         outputStream.write(value.getBytes());
//     }

//     // Mã hóa số nguyên
//     private static void encodeInt(Integer value, ByteArrayOutputStream outputStream) throws IOException {
//         outputStream.write(("i" + value + "e").getBytes());
//     }

//     // Mã hóa Map
//     private static void encodeMap(Map<String, Object> map, ByteArrayOutputStream outputStream) throws IOException {
//         outputStream.write("d".getBytes());
//         for (Map.Entry<String, Object> entry : map.entrySet()) {
//             encodeString(entry.getKey(), outputStream);
//             encode(entry.getValue(), outputStream);
//         }
//         outputStream.write("e".getBytes());
//     }

//     // Mã hóa danh sách
//     private static void encodeList(Object[] list, ByteArrayOutputStream outputStream) throws IOException {
//         outputStream.write("l".getBytes());
//         for (Object obj : list) {
//             encode(obj, outputStream);
//         }
//         outputStream.write("e".getBytes());
//     }

// }

package com.mmt.btl.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.entity.FileOrFolder;

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

    // Hàm để xây dựng cấu trúc cây từ các đường dẫn file
    public static List<FileOrFolder> buildFileTree(MultipartFile[] multipartFiles) {
        Map<String, Object> root = new HashMap<>();
        List<FileOrFolder> fileOrFolders = new ArrayList<>();
    
        for (MultipartFile path : multipartFiles) {
            String[] parts = path.getOriginalFilename().split("/"); // Tách đường dẫn theo dấu "/"
            Map<String, Object> current = root;
            FileOrFolder parentFolder = null;
    
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                FileOrFolder fileOrFolder;
    
                // Kiểm tra xem phần hiện tại đã tồn tại hay chưa
                if (i == parts.length - 1) {
                    // Nếu là phần tử cuối, thêm tên file vào
                    fileOrFolder = FileOrFolder.builder()
                        .fileName(part)
                        .type("FILE")
                        .fileOrFolder(parentFolder)
                        .build();
                    current.putIfAbsent(part, part);
                    fileOrFolders.add(fileOrFolder);
                } else {
                    // Nếu là thư mục, chỉ thêm nếu chưa tồn tại
                    if (!current.containsKey(part)) {
                        fileOrFolder = FileOrFolder.builder()
                            .fileName(part)
                            .type("FOLDER")
                            .fileOrFolder(parentFolder)
                            .build();
                        fileOrFolders.add(fileOrFolder);
                        
                        // Thêm map để lưu các file/thư mục con trong thư mục mới
                        current.put(part, new HashMap<String, Object>());
                    } else {
                        // Nếu đã tồn tại, lấy đối tượng đó làm parentFolder
                        fileOrFolder = findExistingFolder(fileOrFolders, part, parentFolder);
                    }
                    parentFolder = fileOrFolder;
                    current = (Map<String, Object>) current.get(part);
                }
            }
        }
    
        return fileOrFolders;
    }
    
    // Hàm trợ giúp để tìm đối tượng thư mục đã tồn tại với tên và parentFolder nhất định
    private static FileOrFolder findExistingFolder(List<FileOrFolder> fileOrFolders, String part, FileOrFolder parentFolder) {
        return fileOrFolders.stream()
            .filter(f -> f.getFileName().equals(part)
                    && (f.getFileOrFolder() == parentFolder || 
                        (f.getFileOrFolder() != null && f.getFileOrFolder().equals(parentFolder)))
                    && f.getType().equals("FOLDER"))
            .findFirst()
            .orElse(null);
    }
    
}