package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Torrent;

public interface TorrentRepository extends JpaRepository<Torrent, Long> {
}