package com.mmt.btl.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mmt.btl.entity.Tracker;
import com.mmt.btl.response.TrackerResponse;

public interface PeerService {
    public void joinTracker(HttpServletRequest request, List<TrackerResponse> trackers);

    public void disconnectTracker(HttpServletRequest request, TrackerResponse tracker);

    public List<TrackerResponse> getJoinedTrackers(HttpServletRequest request);

    public void printTrackersNotification(List<TrackerResponse> trackers1);
}