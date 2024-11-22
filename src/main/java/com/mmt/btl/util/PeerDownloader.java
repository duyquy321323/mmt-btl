package com.mmt.btl.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmt.btl.controller.WebSocketController;
import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerPiece;
import com.mmt.btl.entity.PeerTorrent;
import com.mmt.btl.entity.Piece;
import com.mmt.btl.entity.Tracker;
import com.mmt.btl.entity.id.PeerPieceId;
import com.mmt.btl.repository.PeerPieceRepository;
import com.mmt.btl.service.impl.FileOrFolderServiceImpl;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class PeerDownloader {

    private PeerPieceRepository peerPieceRepository;

    private WebSocketController webSocketController;

    @Autowired
    public PeerDownloader(PeerPieceRepository peerPieceRepository, WebSocketController webSocketController) {
        this.peerPieceRepository = peerPieceRepository;
        this.webSocketController = webSocketController;
    }

    @Transactional
    public Runnable init(Tracker tracker, FileOrFolder fileOrFolder, Peer currentPeer, Peer peer, List<Piece> pieces,
            ChunkQueueManager chunkQueueManager,
            RandomAccessFile fileAccess) {
        Hibernate.initialize(peer.getPeerPieces());
        Hibernate.initialize(peer.getPeerTorrents());
        return () -> {
            Integer chunkIndex;
            while ((chunkIndex = chunkQueueManager.getNextChunk()) != null) {
                if (peer.getPeerPieces().stream().map(it -> it.getId().getPiece().getId()).collect(Collectors.toList())
                        .contains(pieces.get(chunkIndex).getId())) {
                    // Tính toán vị trí (offset) ghi chunk
                    long offset = chunkIndex * pieces.get(chunkIndex == 0 ? 0 : chunkIndex - 1).getPiece().length;
                    try {
                        fileAccess.seek(offset);
                        // Ghi dữ liệu text vào file
                        fileAccess.write(pieces.get(chunkIndex).getPiece());
                        List<PeerTorrent> pt = peer
                                .getPeerTorrents().stream().filter(item -> Objects
                                        .equals(item.getId().getTorrent().getId(), fileOrFolder.getTorrent().getId()))
                                .collect(Collectors.toList());
                        String typeRole = "LECHER";
                        if (!pt.isEmpty()) {
                            typeRole = pt.get(0).getTypeRole();
                        }
                        String log = FileOrFolderServiceImpl.logger(
                                currentPeer.getId().getUser().getUsername() + ": " + currentPeer.getId().getUserAgent(),
                                fileOrFolder,
                                Double.parseDouble(String.valueOf(pieces.get(chunkIndex).getPiece().length)) / 1024,
                                "Download", String.valueOf(chunkIndex + 1),
                                peer.getId().getUser().getUsername() + ": " + peer.getId().getUserAgent(), typeRole);
                        webSocketController.sendMessageServerToClients(log, currentPeer.getId().getUser().getUsername(), currentPeer.getId().getUserAgent());
                        peerPieceRepository.save(PeerPiece.builder()
                                .id(PeerPieceId.builder().peer(currentPeer).piece(pieces.get(chunkIndex)).build())
                                .build());

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    chunkQueueManager.addChunk(chunkIndex);
                }
            }
        };
    }
}
