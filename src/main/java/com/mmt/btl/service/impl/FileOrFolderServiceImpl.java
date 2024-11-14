package com.mmt.btl.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Piece;
import com.mmt.btl.repository.FileOrFolderRepository;
import com.mmt.btl.repository.PieceRepository;
import com.mmt.btl.service.FileOrFolderService;
import com.mmt.btl.util.FileOrFolderUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileOrFolderServiceImpl implements FileOrFolderService {
    final private FileOrFolderRepository fileOrFolderRepository;
    final private PieceRepository pieceRepository;

    @Override
    @Transactional
    public void upload(MultipartFile[] files) throws NoSuchAlgorithmException, IOException {
        if (files.length > 0) {
            List<FileOrFolder> filesTree = FileOrFolderUtil.buildFileTree(files);
            List<FileOrFolder> filesSave = new ArrayList<>();

            // Duyệt qua từng file/folder từ dưới lên để tính toán hash
            for (int i = filesTree.size() - 1; i >= 0; i--) {
                FileOrFolder fileOrFolder = filesTree.get(i);
                fileOrFolder.setPieces(new ArrayList<>());
                if (fileOrFolder.getFileOrFolder() != null
                        && fileOrFolder.getFileOrFolder().getFileOrFolders() == null) {
                    fileOrFolder.getFileOrFolder().setFileOrFolders(new ArrayList<>());
                }
                if (fileOrFolder.getType().equals("FILE")) {
                    // Tính hash của từng file
                    String filename = fileOrFolder.getFileName();
                    FileOrFolder nextFiles = fileOrFolder.getFileOrFolder();
                    while (nextFiles != null) {
                        filename = nextFiles.getFileName() + "/" + filename;
                        nextFiles = nextFiles.getFileOrFolder();
                    }
                    MultipartFile multipartFile = findMultipartFileByName(files, filename);
                    String fileHash = calculateFileHash(multipartFile, fileOrFolder);
                    fileOrFolder.setHashPieces(fileHash); // Lưu hash của file vào đối tượng FileOrFolder
                    fileOrFolder.setLength(multipartFile.getSize());
                    if (fileOrFolder.getFileOrFolder() != null) {
                        fileOrFolder.getFileOrFolder().getFileOrFolders().add(fileOrFolder);
                    }
                } else if (fileOrFolder.getType().equals("FOLDER")) {
                    // Tính hash của folder bằng cách nối các hash của file/folder con
                    String folderHash = calculateFolderHash(fileOrFolder);
                    fileOrFolder.setHashPieces(folderHash); // Lưu hash của folder
                    if (fileOrFolder.getFileOrFolder() != null) {
                        fileOrFolder.getFileOrFolder().getFileOrFolders().add(fileOrFolder);
                    }
                }
                filesSave.add(fileOrFolder);
            }
            fileOrFolderRepository.saveAll(filesSave);
        }
    }

    // Tìm MultipartFile từ danh sách files theo tên
    private MultipartFile findMultipartFileByName(MultipartFile[] files, String fileName) {
        for (MultipartFile file : files) {
            if (file.getOriginalFilename().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    // Tính hash cho từng file bằng cách chia thành các piece
    @Transactional
    private String calculateFileHash(MultipartFile file, FileOrFolder fileOrFolder)
            throws NoSuchAlgorithmException, IOException {
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[512 * 1024]; // 512KB chunk size
            int bytesRead;
            StringBuilder fileHashBuilder = new StringBuilder();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                byte[] pieceHash = sha1Digest.digest(chunk);
                String hash = bytesToHex(pieceHash);
                fileHashBuilder.append(hash); // Nối hash của piece vào chuỗi file hash
                Piece newPiece = Piece.builder().hash(hash).piece(pieceHash).build();
                fileOrFolder.getPieces().add(newPiece);
                if (pieceRepository.findById(hash).isEmpty()) {
                    pieceRepository.save(newPiece);
                }
            }
            return fileHashBuilder.toString(); // Chuỗi hash đại diện cho toàn bộ file
        }
    }

    // Tính hash cho folder bằng cách nối hash của các file/folder con
    private String calculateFolderHash(FileOrFolder folder) {
        StringBuilder folderHashBuilder = new StringBuilder();
        for (FileOrFolder child : folder.getFileOrFolders()) {
            folderHashBuilder.append(child.getHashPieces()); // Nối hash của các file/folder con
            folder.setLength((folder.getLength() == null ? 0 : folder.getLength()) + child.getLength());
        }
        return folderHashBuilder.toString();
    }

    // Chuyển byte array sang chuỗi hex
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}