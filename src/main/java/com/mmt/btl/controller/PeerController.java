package com.mmt.btl.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mmt.btl.entity.Tracker;
import com.mmt.btl.modelmapper.TrackerResponseModelMapper;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.response.TrackerResponse;
import com.mmt.btl.service.PeerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/peer")
@RequiredArgsConstructor
public class PeerController {

    final private PeerService peerService;

    final private TrackerRepository trackerRepository;

    final private TrackerResponseModelMapper trackerResponseModelMapper;

    @PostMapping("/join-trackers")
    public ResponseEntity<?> joiningTracker(HttpServletRequest request, @RequestBody List<TrackerResponse> trackers) {
        peerService.joinTracker(request, trackers);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/disconnect-trackers")
    public ResponseEntity<?> disconnectTracker(HttpServletRequest request, @RequestBody TrackerResponse tracker) {
        peerService.disconnectTracker(request, tracker);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/join-tracker")
    public ResponseEntity<?> joinTracker(HttpServletRequest request, @RequestParam("hostname") String hostname, @RequestParam("port") Long port){
        List<Tracker> tracker = trackerRepository.findByHostnameAndPort(hostname, port);
        if(!tracker.isEmpty()){
            List<TrackerResponse> response = tracker.stream().map(item -> trackerResponseModelMapper.fromTracker(item)).collect(Collectors.toList());
            peerService.joinTracker(request, response);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/joined-trackers")
    public ResponseEntity<?> getJoinedTrackers(HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(peerService.getJoinedTrackers(request));
    }

    @PostMapping("/notification-trackers")
    public ResponseEntity<?> getNotification(@RequestBody List<TrackerResponse> trackers){
        peerService.printTrackersNotification(trackers);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}