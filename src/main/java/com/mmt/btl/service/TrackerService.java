package com.mmt.btl.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mmt.btl.response.TrackerResponse;

public interface TrackerService {
    public List<TrackerResponse> getAllTracker();

    public List<TrackerResponse> getMyTrackers(HttpServletRequest request);
}