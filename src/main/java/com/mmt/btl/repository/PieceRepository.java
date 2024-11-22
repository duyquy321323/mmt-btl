package com.mmt.btl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mmt.btl.entity.Piece;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    public Optional<Piece> findByHash(String hash);

    // @Query("SELECT p FROM Piece p WHERE :pieces LIKE %p.hash%")
    // public List<Piece> findByContainPieces(@Param("pieces") String pieces);
}