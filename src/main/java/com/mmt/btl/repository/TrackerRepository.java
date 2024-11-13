package com.mmt.btl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mmt.btl.entity.Tracker;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {
}