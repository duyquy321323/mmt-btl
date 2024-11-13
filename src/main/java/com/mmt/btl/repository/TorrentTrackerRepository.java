package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.TorrentTracker;
import com.mmt.btl.entity.id.TorrentTrackerId;

public interface TorrentTrackerRepository extends JpaRepository<TorrentTracker, TorrentTrackerId> {
}