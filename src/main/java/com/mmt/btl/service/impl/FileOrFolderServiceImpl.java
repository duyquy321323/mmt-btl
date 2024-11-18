package com.mmt.btl.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mmt.btl.config.MultiSocketServer;
import com.mmt.btl.controller.WebSocketController;
import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.FilesPiece;
import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerPiece;
import com.mmt.btl.entity.PeerTorrent;
import com.mmt.btl.entity.Piece;
import com.mmt.btl.entity.Torrent;
import com.mmt.btl.entity.TorrentTracker;
import com.mmt.btl.entity.Tracker;
import com.mmt.btl.entity.User;
import com.mmt.btl.entity.id.FilesPieceId;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.entity.id.PeerPieceId;
import com.mmt.btl.entity.id.PeerTorrentId;
import com.mmt.btl.entity.id.TorrentTrackerId;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.modelmapper.FileOrFolderResponseModelMapper;
import com.mmt.btl.repository.FileOrFolderRepository;
import com.mmt.btl.repository.PeerPieceRepository;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.PeerTorrentRepository;
import com.mmt.btl.repository.PieceRepository;
import com.mmt.btl.repository.TorrentRepository;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.repository.UserRepository;
import com.mmt.btl.response.FileOrFolderResponse;
import com.mmt.btl.response.TorrentResponse;
import com.mmt.btl.service.FileOrFolderService;
import com.mmt.btl.util.FileOrFolderUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileOrFolderServiceImpl implements FileOrFolderService {
    final private PieceRepository pieceRepository;

    final private PeerRepository peerRepository;

    final private UserRepository userRepository;

    final private FileOrFolderRepository fileOrFolderRepository;

    final private PeerPieceRepository peerPieceRepository;

    final private TorrentRepository torrentRepository;

    final private PeerTorrentRepository peerTorrentRepository;

    final private TrackerRepository trackerRepository;

    final private FileOrFolderResponseModelMapper fileOrFolderResponseModelMapper;

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
        String peerIdStr = (currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent());
        if (files.length > 0) {
            List<FileOrFolder> filesTree = FileOrFolderUtil.buildFileTree(files);
            List<FileOrFolder> filesSave = new ArrayList<>();

            List<Tracker> trackers = trackerRepository.findByIdIn(trackerIds);
            List<PrintWriter> loggerTrackers = trackers.stream().map(item -> MultiSocketServer.getWritersForPort(Integer.parseInt(item.getPort().toString()))).collect(Collectors.toList());
            List<TorrentTracker> torrentTrackers = new ArrayList<>();
            List<PeerTorrent> peerTorrents = new ArrayList<>();

            Torrent newTorrent = Torrent.builder().torrentTrackers(torrentTrackers).pieceLength(new Long(512 * 1024))
                    .fileOrFolders(filesSave)
                    .peerTorrents(peerTorrents)
                    .build();
            peerTorrents.add(PeerTorrent.builder().typeRole("INITIAL_SEEDER")
                    .id(PeerTorrentId.builder().peer(currentPeer).torrent(newTorrent).build()).build());
            for (Tracker tracker : trackers) {
                torrentTrackers.add(TorrentTracker.builder()
                        .id(TorrentTrackerId.builder().tracker(tracker).torrent(newTorrent).build()).build());
            }

            // Duyệt qua từng file/folder từ dưới lên để tính toán hash
            for (int i = filesTree.size() - 1; i >= 0; i--) {
                FileOrFolder fileOrFolder = filesTree.get(i);
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
                    fileOrFolder.setLength(multipartFile.getSize());
                    String fileHash = calculateFileHash(multipartFile, fileOrFolder, currentPeer, loggerTrackers);
                    fileOrFolder.setHashPieces(fileHash); // Lưu hash của file vào đối tượng FileOrFolder
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
                filesSave.add(fileOrFolder);
                // String log = uploadFile(peerIdStr, filesTree.get(i));
                // for (Tracker tracker : trackers) {
                //     webSocketController.sendMessageTrackerToClients(log.substring(0, 22) + " [Tracker " + "http://" + tracker.getHostName() + ":" + tracker.getPort() + "]\n" + log.substring(22));
                // }
            }

            byte[] hashByte = FileOrFolderUtil.calculateSHA1(filesSave.getLast().getHashPieces().getBytes());
            String hashInfo = FileOrFolderUtil.bytesToHex(hashByte);
            newTorrent.setInfoHash(hashInfo);
            newTorrent.setCreateBy(userDetails.getUsername() + ": " + userAgent);
            newTorrent.setCreateDate(new Date());
            newTorrent.setEncoding(request.getCharacterEncoding());
            torrentRepository.save(newTorrent);
            // logOfTrackerUpload(request, trackers, filesSave, currentPeer);
        }
    }

    // Hàm gửi tin nhắn về client của Tracker khi Upload file / folder
    // public void logOfTrackerUpload(HttpServletRequest request, List<Tracker> trackers, List<FileOrFolder> fileOrFolders,
    //         Peer peer) {
    //     for (Tracker tracker : trackers) {
    //         fileOrFolders.sort(null);
    //         Map<String, Object> tree = FileOrFolderUtil.buildDirectoryTree(fileOrFolders);
    //         String typeTree = getMultipartFileType((List<Map<String, Object>>) tree.get("children"));
    //         String peerIdStr = (peer.getId().getUser().getUsername() + ": " + peer.getId().getUserAgent());

    //         String log = uploadFile(peerIdStr, fileOrFolders.get(0));

    //         switch (typeTree) {
    //             case "SINGLE_FILE" -> {
    //                 log = uploadFile(peerIdStr, fileOrFolders.get(0));
    //             }
    //             case "MULTIPLE_FILE" -> {
    //                 log = uploadFiles(peerIdStr, fileOrFolders);
    //             }
    //             case "SINGLE_FOLDER" -> {
    //                 log = uploadFolder(peerIdStr, fileOrFolders.getLast().getFileName(), fileOrFolders);
    //             }
    //             case "FOLDER_OF_FOLDER" -> {
    //                 String ipAddress = request.getHeader("X-Forwarded-For");
    //                 if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
    //                     ipAddress = request.getRemoteAddr();
    //                 }
    //                 log = uploadNestedFolder(peerIdStr, ipAddress, request.getRemotePort(), "fake");
    //             }
    //             default -> {
    //             }
    //         }
    //         String start = log.substring(0, 22) + "\nannounce: " + tracker.getUrl() + "\n" + log.substring(22);
    //         webSocketController.sendMessageTrackerToClients(start);
    //     }
    // }

    // Hàm check loại file mà người dùng up lên
    // private String getMultipartFileType(List<Map<String, Object>> trees) {
    //     if (trees.size() == 1 && trees.get(0).get("type").equals("FILE"))
    //         return "SINGLE_FILE";
    //     else if (trees.get(0).get("type").equals("FILE"))
    //         return "MULTIPLE_FILE";
    //     for (Map<String, Object> tree : trees) {
    //         for (Map<String, Object> subTree : (List<Map<String, Object>>) tree.get("children")) {
    //             if (subTree.get("type").equals("FOLDER")) {
    //                 return "FOLDER_OF_FOLDER";
    //             }
    //         }
    //     }
    //     return "SINGLE_FOLDER";
    // }

    // Log khi upload 1 file
    public String logger(String peerId, FileOrFolder fileOrFolder, double fileSize, String typeLog, String part) {
        StringBuilder logBuilder = new StringBuilder();
        String timestamp = getCurrentTimestamp();
        String fileName = FileOrFolderUtil.getPath(fileOrFolder);
        // String hash = fileOrFolder.getHashPieces(); // Giả sử bạn có một phương thức tạo hash cho file
        // String status = "SUCCESS"; // Giả sử upload thành công

        // Thêm log cho file
        if((fileOrFolder.getLength() / 1024) == fileSize){
        logBuilder.append(String.format(
                "[%s] Peer %s: [peer_id: %s, File: \"%s\", Size: %f.3KB]",
                timestamp, typeLog, peerId, fileName, fileSize));
        }else{
            logBuilder.append(String.format(
                "[%s] Peer %s: [peer_id: %s, File: \"%s\", Part: %s, Size: %f.3KB]",
                timestamp, typeLog, peerId, fileName, part, fileSize));
        }
        return logBuilder.toString();
    }

    // Log khi upload nhiều file
    // public String uploadFiles(String peerId, List<FileOrFolder> files) {
    //     StringBuilder logBuilder = new StringBuilder();
    //     String timestamp = getCurrentTimestamp();
    //     logBuilder.append(String.format("[%s] Peer ID: %s uploaded multiple files:\n", timestamp, peerId));

    //     // Duyệt qua từng file và tạo log
    //     for (FileOrFolder file : files) {
    //         String fileName = file.getFileName();
    //         long fileSize = file.getLength(); // Size in KB
    //         String hash = file.getHashPieces();
    //         String status = "SUCCESS"; // Giả sử upload thành công
    //         logBuilder.append(
    //                 String.format("    - \"%s\" (%dKB), Hash: %s, Status: %s\n", fileName, fileSize, hash, status));
    //     }

    //     logBuilder.append("Tracker Response: Metadata for " + files.size() + " files saved.\n");

    //     return logBuilder.toString();
    // }

    // // Log khi upload 1 thư mục
    // public String uploadFolder(String peerId, String folderName, List<FileOrFolder> files) {
    //     StringBuilder logBuilder = new StringBuilder();
    //     String timestamp = getCurrentTimestamp();
    //     int countFile = 0;
    //     for (FileOrFolder file : files) {
    //         if (file.getType().equals("FILE"))
    //             countFile++;
    //     }
    //     logBuilder.append(String.format("[%s] Peer ID: %s uploaded folder \"%s\" containing %d files:\n",
    //             timestamp, peerId, folderName, countFile));

    //     // Duyệt qua từng file trong thư mục và tạo log
    //     for (FileOrFolder file : files) {
    //         if (file.getType().equals("FOLDER"))
    //             continue;
    //         String fileName = FileOrFolderUtil.getPath(file);
    //         long fileSize = file.getLength(); // Size in KB
    //         String hash = file.getHashPieces();
    //         String status = "SUCCESS"; // Giả sử upload thành công
    //         logBuilder.append(
    //                 String.format("    - \"%s\" (%dKB), Hash: %s, Status: %s\n", fileName, fileSize, hash, status));
    //     }

    //     logBuilder.append(String.format(
    //             "Tracker Response: Metadata for folder '%s' saved. All files successfully processed.\n", folderName));
    //     return logBuilder.toString();
    // }

    // // Log khi upload thư mục lồng nhau
    // public String uploadNestedFolder(String peerId, String ip, int port, String folderStructure) {
    //     StringBuilder logBuilder = new StringBuilder();
    //     String timestamp = getCurrentTimestamp();

    //     logBuilder.append(String.format("[%s] Peer %s - IP: %s, Port: %d\n", timestamp, peerId, ip, port));
    //     logBuilder.append("Action: Upload Folder\n");
    //     logBuilder.append("Folder Structure:\n");
    //     logBuilder.append(folderStructure); // Dữ liệu cấu trúc thư mục đã được tạo sẵn
    //     logBuilder.append("Status: Success\n");

    //     return logBuilder.toString();
    // }

    // Tạo timestamp cho log
    private String getCurrentTimestamp() {
        // Trả về timestamp theo định dạng "yyyy-MM-dd HH:mm:ss"
        return java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
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
    private String calculateFileHash(MultipartFile file, FileOrFolder fileOrFolder, Peer peer, List<PrintWriter> loggers)
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
                String peerId = peer.getId().getUser().getUsername() + ": " + peer.getId().getUserAgent();
                for(PrintWriter log : loggers){
                    log.println(logger(peerId, fileOrFolder, newPiece.getPiece().length, "Update", String.valueOf(identity + 1)));
                }
                identity++;
            }
            return fileHashBuilder.toString();
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

    @Override
    public List<TorrentResponse> getUploadedFile(HttpServletRequest request) {
        UserDetails userDetails = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new MMTNotFoundException("User Not Found...!"));
                String userAgent = request.getHeader("User-Agent");
                Peer currentPeer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                        .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
                List<PeerTorrent> peerTorrents = peerTorrentRepository.findByIdPeerAndTypeRole(currentPeer,
                        "INITIAL_SEEDER");
                List<Long> ids = peerTorrents.stream()
                        .map(item -> {
                            Long torrentId = item.getId().getTorrent().getId();
                            return torrentId;
                        })
                        .collect(Collectors.toList());
                List<Torrent> torrents = torrentRepository.findByIdIn(ids);
                List<TorrentResponse> responses = new ArrayList<>();
                List<List<FileOrFolderResponse>> listFileOrFolders = new ArrayList<>();
                for (Torrent torrent : torrents) {
                    List<FileOrFolder> fileOrFolders = fileOrFolderRepository.findAllByTorrent(torrent);
                    List<FileOrFolderResponse> fileOrFolderResponses = fileOrFolders.stream()
                            .map(it -> fileOrFolderResponseModelMapper.fromFileOrFolder(it))
                            .collect(Collectors.toList());
                    String pieces = "";
                    String piecesFile = "";
                    for (FileOrFolder file : fileOrFolders) {
                        piecesFile += file.getHashPieces();
                        if (file.getType().equals("FOLDER")) {
                            if (file.getFileOrFolder() == null) {
                                pieces = file.getHashPieces();
                                break;
                            }
                        }
                    }
                    Map<String, Object> treeFiles = FileOrFolderUtil.buildDirectoryTree(fileOrFolders);
                    TorrentResponse tr = TorrentResponse.builder().fileOrFolders(fileOrFolderResponses)
                            .createBy(torrent.getCreateBy())
                            .createDate(torrent.getCreateDate()).hashInfo(torrent.getInfoHash()).treeFiles(treeFiles).encoding(torrent.getEncoding())
                            .announce(torrent.getTorrentTrackers().stream().map(it -> {
                                return "http://" + it.getId().getTracker().getHostname() + ":" + it.getId().getTracker().getPort();
                            }).collect(Collectors.toList())).build();
                    if (pieces.equals("")) {
                        tr.setPieces(piecesFile);
                    } else {
                        tr.setPieces(pieces);
                    }
                    responses.add(tr);
                }
                return responses;
            }
        }
        throw new MMTNotFoundException("User Not Found...!");
    }
}