package com.mmt.btl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Torrent;

public interface TorrentRepository extends JpaRepository<Torrent, Long> {
    public List<Torrent> findByIdIn(List<Long> ids);

    public Optional<Torrent> findByInfoHash(String infoHash);
}