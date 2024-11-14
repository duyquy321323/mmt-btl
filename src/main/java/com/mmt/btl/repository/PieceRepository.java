package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Piece;

public interface PieceRepository extends JpaRepository<Piece, String> {
}