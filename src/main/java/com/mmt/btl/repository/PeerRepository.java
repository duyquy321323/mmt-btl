package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.id.PeerId;

public interface PeerRepository extends JpaRepository<Peer, PeerId> {
    public List<Peer> findAllByStatus(Boolean status);
}