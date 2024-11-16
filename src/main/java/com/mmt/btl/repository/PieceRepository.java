package com.mmt.btl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Piece;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    public Optional<Piece> findByHash(String hash);
}