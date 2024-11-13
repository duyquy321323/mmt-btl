package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Piece;
import com.mmt.btl.entity.id.PieceId;

public interface PieceRepository extends JpaRepository<Piece, PieceId> {
}