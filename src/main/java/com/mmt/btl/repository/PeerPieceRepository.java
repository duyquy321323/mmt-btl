package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.PeerPiece;
import com.mmt.btl.entity.id.PeerPieceId;

public interface PeerPieceRepository extends JpaRepository<PeerPiece, PeerPieceId> {
}