package com.mmt.btl.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mmt.btl.controller.WebSocketController;
import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.FilesPiece;
import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerPiece;
import com.mmt.btl.entity.PeerTorrent;
import com.mmt.btl.entity.PeerTracker;
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
import com.mmt.btl.request.DownloadRequest;
import com.mmt.btl.response.FileOrFolderResponse;
import com.mmt.btl.response.TorrentResponse;
import com.mmt.btl.service.FileOrFolderService;
import com.mmt.btl.util.ChunkQueueManager;
import com.mmt.btl.util.FileOrFolderUtil;
import com.mmt.btl.util.PeerDownloader;

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

    final private WebSocketController webSocketController;

    final private FileOrFolderResponseModelMapper fileOrFolderResponseModelMapper;

    final private PeerDownloader peerDownloader;

    @SuppressWarnings("removal")
    @Override
    @Transactional
    public void upload(HttpServletRequest request, MultipartFile[] files, List<Long> trackerIds)
            throws NoSuchAlgorithmException, IOException {
        String userAgent = request.getHeader("User-Agent");
        UserDetails userDetails = null;
        Peer peerStart = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) authentication.getPrincipal();
        }
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            if (user != null && userAgent != null) {
                peerStart = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                        .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            }
        }
        final Peer currentPeer = peerStart;
        if (files.length > 0) {
            List<FileOrFolder> filesTree = FileOrFolderUtil.buildFileTree(files);
            List<FileOrFolder> filesSave = new ArrayList<>();

            List<Tracker> trackers = trackerRepository.findByIdIn(trackerIds);
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
                    String fileHash = calculateFileHash(multipartFile, fileOrFolder, currentPeer, trackers);
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
            }

            for (Tracker tracker : trackers) {
                if (tracker.getPeerTrackers().stream().filter(it -> it.getId().getPeer().getId().getUser().getUsername()
                        .equals(currentPeer.getId().getUser().getUsername())
                        && it.getId().getPeer().getId().getUserAgent().equals(currentPeer.getId().getUserAgent()))
                        .collect(Collectors.toList()).isEmpty()) {
                    for (PeerTorrent peerTorrent1 : currentPeer.getPeerTorrents()) {
                        FileOrFolder lastFile = peerTorrent1.getId().getTorrent().getFileOrFolders().getLast();
                        if (lastFile.getType().equals("FILE")) {
                            for (FileOrFolder file : peerTorrent1.getId().getTorrent().getFileOrFolders()) {
                                String log = logger(
                                        currentPeer.getId().getUser().getUsername() + ": "
                                                + currentPeer.getId().getUserAgent(),
                                        file, Double.parseDouble(String.valueOf(file.getLength())) / 1024, "Sharing",
                                        null, null, peerTorrent1.getTypeRole());
                                webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                            }
                        } else {
                            String log = logger(
                                    currentPeer.getId().getUser().getUsername() + ": "
                                            + currentPeer.getId().getUserAgent(),
                                    lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024,
                                    "Sharing", null, null, peerTorrent1.getTypeRole());
                            webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                        }
                    }
                }
                FileOrFolder lastFile = filesSave.getLast();
                if (lastFile.getType().equals("FILE")) {
                    for (FileOrFolder file : filesSave) {
                        String log = logger(
                                currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent(),
                                file, Double.parseDouble(String.valueOf(file.getLength())) / 1024, "Sharing", null,
                                null, "INITIAL_SEEDER");
                        webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                    }
                } else {
                    String log = logger(
                            currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent(),
                            lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024, "Sharing", null,
                            null, "INITIAL_SEEDER");
                    webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                }
            }

            newTorrent.setCreateBy(userDetails.getUsername() + ": " + userAgent);
            newTorrent.setCreateDate(new Date());
            byte[] hashByte = FileOrFolderUtil.calculateSHA1(
                    (filesSave.getLast().getHashPieces() + newTorrent.getCreateDate().toString()).getBytes());
            String hashInfo = FileOrFolderUtil.bytesToHex(hashByte);
            newTorrent.setInfoHash(hashInfo);
            newTorrent.setEncoding(request.getCharacterEncoding());
            torrentRepository.save(newTorrent);

            FileOrFolder lastFile = filesSave.getLast();
                if (lastFile.getType().equals("FILE")) {
                    for (FileOrFolder file : filesSave) {
                        String log = logger(
                                currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent(),
                                file, Double.parseDouble(String.valueOf(file.getLength())) / 1024, "Finished upload", null,
                                null, null);
                        webSocketController.sendMessageServerToClients(log, userDetails.getUsername(), userAgent);
                    }
                } else {
                    String log = logger(
                                currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent(),
                                lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024, "Finished upload", null,
                                null, null);
                            webSocketController.sendMessageServerToClients(log, userDetails.getUsername(), userAgent);
                }
        }
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Chuyển chữ cái đầu thành chữ hoa và phần còn lại giữ nguyên
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    // khi upload 1 file
    public static String logger(String peerId, FileOrFolder fileOrFolder, double fileSize, String typeLog,
            String part, String peerSeeding, String typeRole) {
        StringBuilder logBuilder = new StringBuilder();
        String timestamp = getCurrentTimestamp();
        String fileName = FileOrFolderUtil.getPath(fileOrFolder);
        if (typeLog.equals("Upload")) {
            if ((Double.parseDouble(String.valueOf(fileOrFolder.getLength())) / 1024) == fileSize) {
                logBuilder.append(String.format(
                        "[%s] Peer %s: [peer_id: %s, File: \"%s\", Size: %.3fKB]",
                        timestamp, typeLog, peerId, fileName, fileSize));
            } else {
                logBuilder.append(String.format(
                        "[%s] Peer %s: [peer_id: %s, File: \"%s\", Part: %s, Size: %.3fKB]",
                        timestamp, typeLog, peerId, fileName, part, fileSize));
            }
        } else if (typeLog.equals("Joined") || typeLog.equals("Disconnected")) {
            logBuilder.append(String.format(
                    "[%s] Peer %s: [peer_id: %s]",
                    timestamp, typeLog, peerId));
        } else if (typeLog.equals("Download")) {
            if ((Double.parseDouble(String.valueOf(fileOrFolder.getLength())) / 1024) == fileSize) {
                logBuilder.append(String.format(
                        "[%s] Peer %sing: [peer_id: %s, File: \"%s\", Size: %.3fKB]\n%s: [peer_id: %s]",
                        timestamp, typeLog, peerId, fileName, fileSize, capitalizeFirstLetter(typeRole), peerSeeding));
            } else {
                logBuilder.append(String.format(
                        "[%s] Peer %sing: [peer_id: %s, File: \"%s\", Part: %s, Size: %.3fKB]\n%s: [peer_id: %s]",
                        timestamp, typeLog, peerId, fileName, part, fileSize, capitalizeFirstLetter(typeRole),
                        peerSeeding));
            }
        } else if (typeLog.equals("Sharing")) {
            logBuilder.append(String.format(
                    "[%s] %s file: [peer_id: %s, File: \"%s\", Size: %.3fKB] as %s",
                    timestamp, typeLog, peerId, fileName, fileSize, capitalizeFirstLetter(typeRole)));
        } else if(typeLog.equals("Finished download")){
            logBuilder.append(String.format(
                        "[%s] Peer %s: [peer_id: %s, %s: \"%s\", Size: %.3fKB]",
                        timestamp, typeLog, peerId, capitalizeFirstLetter(fileOrFolder.getType()), fileName, fileSize));
        } else if(typeLog.equals("Finished upload")){
            logBuilder.append(String.format(
                        "[%s] Peer %s: [peer_id: %s, %s: \"%s\", Size: %.3fKB]",
                        timestamp, typeLog, peerId,capitalizeFirstLetter(fileOrFolder.getType()), fileName,  fileSize));
        }
        return logBuilder.toString();
    }

    // timestamp cho log
    private static String getCurrentTimestamp() {
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
    private String calculateFileHash(MultipartFile file, FileOrFolder fileOrFolder, Peer peer, List<Tracker> trackers)
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
                Piece newPiece = Piece.builder().hash(hash).piece(chunk).build();
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
                for (Tracker tracker : trackers) {
                    String mes = logger(peerId, fileOrFolder,
                            Double.parseDouble(String.valueOf(newPiece.getPiece().length)) / 1024, "Upload",
                            String.valueOf(identity + 1), null, null);
                    webSocketController.sendMessageServerToClients(mes, peer.getId().getUser().getUsername(),
                            peer.getId().getUserAgent());
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
        UserDetails userDetails;
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
                            .createDate(torrent.getCreateDate()).hashInfo(torrent.getInfoHash()).treeFiles(treeFiles)
                            .encoding(torrent.getEncoding())
                            .announce(torrent.getTorrentTrackers().stream().map(it -> {
                                return "http://" + it.getId().getTracker().getHostname() + ":"
                                        + it.getId().getTracker().getPort();
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

    @Override
    public List<TorrentResponse> getDownloadFile(HttpServletRequest request) {
        UserDetails userDetails;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new MMTNotFoundException("User Not Found...!"));
                String userAgent = request.getHeader("User-Agent");
                Peer currentPeer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                        .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
                List<PeerTracker> peerTrackers = currentPeer.getPeerTrackers();
                List<Tracker> trackers = peerTrackers.stream().map(item -> item.getId().getTracker())
                        .collect(Collectors.toList());
                List<Peer> peers = new ArrayList<>();
                trackers.stream().map(item -> peers.addAll(item.getPeerTrackers().stream().filter(it -> {
                    return (it.getId().getPeer().getId().getUserAgent() == null
                            ? currentPeer.getId().getUserAgent() != null
                            : !it.getId().getPeer().getId().getUserAgent().equals(currentPeer.getId().getUserAgent()))
                            || !Objects.equals(it.getId().getPeer().getId().getUser().getId(),
                                    currentPeer.getId().getUser().getId());
                }).collect(Collectors.toList()).stream().map(it -> it.getId().getPeer()).collect(Collectors.toList())))
                        .collect(Collectors.toList());
                List<Torrent> torrents = new ArrayList<>();
                for (Peer peer : peers) {
                    torrents.addAll(peer.getPeerTorrents().stream().filter(item -> {
                        return !torrents.stream().map(it -> it.getId()).collect(Collectors.toList())
                                .contains(item.getId().getTorrent().getId());
                    }).collect(Collectors.toList()).stream().map(item -> item.getId().getTorrent())
                            .collect(Collectors.toList()));
                }
                List<TorrentResponse> responses = new ArrayList<>();
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
                            .createDate(torrent.getCreateDate()).hashInfo(torrent.getInfoHash()).treeFiles(treeFiles)
                            .encoding(torrent.getEncoding())
                            .announce(torrent.getTorrentTrackers().stream().map(it -> {
                                return "http://" + it.getId().getTracker().getHostname() + ":"
                                        + it.getId().getTracker().getPort();
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

    @Override
    @Transactional
    public void download(HttpServletRequest request, DownloadRequest downloadRequest) {
        String userAgent = request.getHeader("User-Agent");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            Peer peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                    .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            Torrent torrent = torrentRepository.findByInfoHash(downloadRequest.getHashInfo())
                    .orElseThrow(() -> new MMTNotFoundException("Torrent Not Found...!"));
            List<Tracker> trackers = torrent.getTorrentTrackers().stream().map(it -> it.getId().getTracker())
                    .collect(Collectors.toList());
            List<Tracker> joinedTrackers = peer.getPeerTrackers().stream().map(it -> it.getId().getTracker())
                    .collect(Collectors.toList());
            List<Tracker> currentTrackers = trackers.stream().filter(item -> {
                for (Tracker tk : joinedTrackers) {
                    if (tk.getHostname().equals(item.getHostname()) && tk.getPort().equals(item.getPort())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            for (FileOrFolderResponse file : downloadRequest.getFileOrFolders()) {
                // List<Piece> pieces =
                // pieceRepository.findByContainPieces(downloadRequest.getPieces());
                if (file.getType().equals("FILE") && file.getPath().startsWith(downloadRequest.getPath())) {
                    try {
                        FileOrFolder fileOrFolder = getFileOrFolder(torrent, file.getPath());
                        List<FilesPiece> fp = fileOrFolder.getFilesPieces();
                        List<Piece> pieces = fp.stream().map((it) -> {
                            return it.getId().getPiece();
                        }).collect(Collectors.toList());
                        ChunkQueueManager chunkQueueManager = new ChunkQueueManager(pieces.size());
                        File dir = new File("download/" + (file.getPath().indexOf('/') != -1
                                ? file.getPath().substring(0, file.getPath().lastIndexOf("/"))
                                : ""));
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        RandomAccessFile fileAccess = new RandomAccessFile("download/" + file.getPath(), "rw");
                        // for (int i = 0; i < fileOrFolder.getLength(); i++) {
                        // fileAccess.write(' '); // Ghi các ký tự trống
                        // }
                        for (Tracker ctracker : currentTrackers) {
                            List<Peer> peersOfTracker;
                            peersOfTracker = ctracker.getPeerTrackers().stream()
                                    .map(it -> it.getId().getPeer()).collect(Collectors.toList()).stream()
                                    .filter(it -> (it.getId().getUser().getUsername() == null
                                            ? peer.getId().getUser().getUsername() != null
                                            : !it.getId().getUser().getUsername()
                                                    .equals(peer.getId().getUser().getUsername()))
                                            || (it.getId().getUserAgent() == null ? peer.getId().getUserAgent() != null
                                                    : !it.getId().getUserAgent().equals(peer.getId().getUserAgent())))
                                    .collect(Collectors.toList());
                            ExecutorService executor = Executors.newFixedThreadPool(peersOfTracker.size());
                            for (Peer p : peersOfTracker) {
                                executor.execute(peerDownloader.init(ctracker, fileOrFolder, peer, p, pieces,
                                        chunkQueueManager, fileAccess));
                            }
                            executor.shutdown();
                        }
                        peerTorrentRepository.save(
                                PeerTorrent.builder().id(PeerTorrentId.builder().peer(peer).torrent(torrent).build())
                                        .typeRole("SEEDER").build());
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            List<FileOrFolder> filesSave = downloadRequest.getFileOrFolders().stream().map(it -> getFileOrFolder(torrent, it.getPath())).collect(Collectors.toList());
            FileOrFolder lastFile = filesSave.getLast();
                if (lastFile.getType().equals("FILE")) {
                    for (FileOrFolderResponse file : downloadRequest.getFileOrFolders()) {
                        if(file.getPath().startsWith(downloadRequest.getPath())){
                        try {
                            String log = logger(
                                    peer.getId().getUser().getUsername() + ": " + peer.getId().getUserAgent(),
                                    getFileOrFolder(torrent, file.getPath()), Double.parseDouble(String.valueOf(getFileOrFolder(torrent, file.getPath()).getLength())) / 1024, "Finished download", null,
                                    null, null);
                            webSocketController.sendMessageServerToClients(log, peer.getId().getUser().getUsername(), peer.getId().getUserAgent());
                        } catch (MessagingException ex) {
                        } catch (JsonProcessingException ex) {
                        }
                    }
                    }
                } else {
                    String log = logger(
                        peer.getId().getUser().getUsername() + ": " + peer.getId().getUserAgent(),
                                lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024, "Finished download", null,
                                null, null);
                try {
                    webSocketController.sendMessageServerToClients(log, peer.getId().getUser().getUsername(), peer.getId().getUserAgent());
                } catch (MessagingException ex) {
                } catch (JsonProcessingException ex) {
                }
                }
        }
    }

    private FileOrFolder getFileOrFolder(Torrent torrent, String path) {
        return getFileOrFolder(torrent, path, null, 0);
    }

    private FileOrFolder getFileOrFolder(Torrent torrent, String path, FileOrFolder parent, int level) {
        String[] fileNames = path.split("/");
        if (fileNames.length <= level)
            return parent;
        String filename = fileNames[level];
        List<FileOrFolder> fileOrFolders = torrent.getFileOrFolders();
        FileOrFolder par = fileOrFolders.stream().filter(item -> {
            if (parent != null)
                return (item.getFileName() == null ? filename == null : item.getFileName().equals(filename))
                        && Objects.equals(item.getFileOrFolder().getId(), parent.getId());
            return (item.getFileName() == null ? filename == null : item.getFileName().equals(filename))
                    && item.getFileOrFolder() == null;
        }).collect(Collectors.toList()).get(0);
        return getFileOrFolder(torrent, path, par, level + 1);
    }

    @Override
    public List<TorrentResponse> getDownloadHistoryFile(HttpServletRequest request){
        UserDetails userDetails;
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
                        "SEEDER");
                List<Long> ids = peerTorrents.stream()
                        .map(item -> {
                            Long torrentId = item.getId().getTorrent().getId();
                            return torrentId;
                        })
                        .collect(Collectors.toList());
                List<Torrent> torrents = torrentRepository.findByIdIn(ids);
                List<TorrentResponse> responses = new ArrayList<>();
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
                            .createDate(torrent.getCreateDate()).hashInfo(torrent.getInfoHash()).treeFiles(treeFiles)
                            .encoding(torrent.getEncoding())
                            .announce(torrent.getTorrentTrackers().stream().map(it -> {
                                return "http://" + it.getId().getTracker().getHostname() + ":"
                                        + it.getId().getTracker().getPort();
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