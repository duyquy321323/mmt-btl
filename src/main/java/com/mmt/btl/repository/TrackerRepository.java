package com.mmt.btl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Tracker;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {
    public List<Tracker> findByIdIn(List<Long> ids);
}