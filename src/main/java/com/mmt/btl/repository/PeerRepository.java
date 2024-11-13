package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.id.PeerId;

public interface PeerRepository extends JpaRepository<Peer, PeerId> {
    // @Query("SELECT p FROM Peer p WHERE p.userAgent = :userAgent AND p.user = :user ")
    // Optional<Peer> findBy(@Param("userAgent") String userAgent, @Param("user") User user);
}