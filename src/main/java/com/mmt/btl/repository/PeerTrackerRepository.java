package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.PeerTracker;
import com.mmt.btl.entity.id.PeerTrackerId;

public interface PeerTrackerRepository extends JpaRepository<PeerTracker, PeerTrackerId> {
    public void deleteByIdIn(List<PeerTrackerId> ids);
}