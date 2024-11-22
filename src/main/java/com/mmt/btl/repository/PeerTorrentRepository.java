package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerTorrent;
import com.mmt.btl.entity.id.PeerTorrentId;

public interface PeerTorrentRepository extends JpaRepository<PeerTorrent, PeerTorrentId> {
    public List<PeerTorrent> findByIdPeerAndTypeRole(Peer peer, String typeRole);
}