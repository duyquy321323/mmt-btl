package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.TorrentTracker;
import com.mmt.btl.entity.Tracker;
import com.mmt.btl.entity.id.TorrentTrackerId;

public interface TorrentTrackerRepository extends JpaRepository<TorrentTracker, TorrentTrackerId> {
    public List<TorrentTracker> findByIdTracker(Tracker tracker); 
}