package com.mmt.btl.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.FilesPiece;
import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerFile;
import com.mmt.btl.entity.PeerPiece;
import com.mmt.btl.entity.Piece;
import com.mmt.btl.entity.Torrent;
import com.mmt.btl.entity.TorrentTracker;
import com.mmt.btl.entity.Tracker;
import com.mmt.btl.entity.User;
import com.mmt.btl.entity.id.FilesPieceId;
import com.mmt.btl.entity.id.PeerFileId;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.entity.id.PeerPieceId;
import com.mmt.btl.entity.id.TorrentTrackerId;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.repository.PeerPieceRepository;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.PieceRepository;
import com.mmt.btl.repository.TorrentRepository;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.repository.UserRepository;
import com.mmt.btl.service.FileOrFolderService;
import com.mmt.btl.util.FileOrFolderUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileOrFolderServiceImpl implements FileOrFolderService {
    final private PieceRepository pieceRepository;

    final private PeerRepository peerRepository;

    final private UserRepository userRepository;

    final private PeerPieceRepository peerPieceRepository;

    final private TorrentRepository torrentRepository;

    final private TrackerRepository trackerRepository;

    @SuppressWarnings("removal")
    @Override
    @Transactional
    public void upload(HttpServletRequest request, MultipartFile[] files, List<Long> trackerIds)
            throws NoSuchAlgorithmException, IOException {
        String userAgent = request.getHeader("User-Agent");
        UserDetails userDetails = null;
        Peer currentPeer = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) authentication.getPrincipal();
        }
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            if (user != null && userAgent != null) {
                currentPeer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                        .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            }
        }
        if (files.length > 0) {
            List<FileOrFolder> filesTree = FileOrFolderUtil.buildFileTree(files);
            List<FileOrFolder> filesSave = new ArrayList<>();

            List<Tracker> trackers = trackerRepository.findByIdIn(trackerIds);
            List<TorrentTracker> torrentTrackers = new ArrayList<>();
            Torrent newTorrent = Torrent.builder().torrentTrackers(torrentTrackers).pieceLength(new Long(512 * 1024))
                    .fileOrFolders(filesSave).build();
            for (Tracker tracker : trackers) {
                torrentTrackers.add(TorrentTracker.builder()
                        .id(TorrentTrackerId.builder().tracker(tracker).torrent(newTorrent).build()).build());
            }

            // Duyệt qua từng file/folder từ dưới lên để tính toán hash
            for (int i = filesTree.size() - 1; i >= 0; i--) {
                FileOrFolder fileOrFolder = filesTree.get(i);
                fileOrFolder.setPeerFiles(new ArrayList<>());
                fileOrFolder.setFilesPieces(new ArrayList<>());
                fileOrFolder.setTorrent(newTorrent);
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
                    String fileHash = calculateFileHash(multipartFile, fileOrFolder, currentPeer);
                    fileOrFolder.setHashPieces(fileHash); // Lưu hash của file vào đối tượng FileOrFolder
                    fileOrFolder.setLength(multipartFile.getSize());
                    if (fileOrFolder.getFileOrFolder() != null) {
                        fileOrFolder.getFileOrFolder().getFileOrFolders().add(fileOrFolder);
                    }
                } else if (fileOrFolder.getType().equals("FOLDER")) {
                    String folderHash = calculateFolderHash(fileOrFolder);
                    fileOrFolder.setHashPieces(folderHash); // Lưu hash của folder
                    if (fileOrFolder.getFileOrFolder() != null) {
                        fileOrFolder.getFileOrFolder().getFileOrFolders().add(fileOrFolder);
                    }
                }
                fileOrFolder.getPeerFiles().add(PeerFile.builder()
                        .id(PeerFileId.builder().fileOrFolder(fileOrFolder).peer(currentPeer).build()).build());
                filesSave.add(fileOrFolder);
            }
            // fileOrFolderRepository.saveAll(filesSave);
            torrentRepository.save(newTorrent);
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
    private String calculateFileHash(MultipartFile file, FileOrFolder fileOrFolder, Peer peer)
            throws NoSuchAlgorithmException, IOException {
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[512 * 1024]; // 512KB chunk size
            StringBuilder fileHashBuilder = new StringBuilder();
            int bytesRead;
            long identity = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                byte[] pieceHash = sha1Digest.digest(chunk);
                String hash = bytesToHex(pieceHash);
                fileHashBuilder.append(hash); // Nối hash của piece vào chuỗi file hash
                Piece newPiece = Piece.builder().hash(hash).piece(pieceHash).build();
                if (pieceRepository.findByHash(hash).isEmpty()) {
                    newPiece.setPeerPieces(new ArrayList<>());
                    newPiece.getPeerPieces()
                            .add(PeerPiece.builder().id(PeerPieceId.builder().peer(peer).piece(newPiece).build())
                                    .build());

                } else {
                    newPiece = pieceRepository.findByHash(hash)
                            .orElseThrow(() -> new MMTNotFoundException("Piece Not Found...!"));
                    if (peerPieceRepository.findById(PeerPieceId.builder().peer(peer).piece(newPiece).build())
                            .isEmpty()) {
                        newPiece.getPeerPieces()
                                .add(PeerPiece.builder().id(PeerPieceId.builder().peer(peer).piece(newPiece).build())
                                        .build());

                    }
                }
                fileOrFolder.getFilesPieces().add(FilesPiece.builder().identity(identity)
                        .id(FilesPieceId.builder().piece(newPiece).fileOrFolder(fileOrFolder).build()).build());
                pieceRepository.save(newPiece);
                identity++;
            }
            return fileHashBuilder.toString(); // 
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