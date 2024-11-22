package com.mmt.btl.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mmt.btl.controller.WebSocketController;
import com.mmt.btl.entity.FileOrFolder;
import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.PeerTorrent;
import com.mmt.btl.entity.PeerTracker;
import com.mmt.btl.entity.Tracker;
import com.mmt.btl.entity.User;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.entity.id.PeerTrackerId;
import com.mmt.btl.exception.LoginFailedException;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.modelmapper.TrackerResponseModelMapper;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.PeerTrackerRepository;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.repository.UserRepository;
import com.mmt.btl.response.TrackerResponse;
import com.mmt.btl.service.PeerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PeerServiceImpl implements PeerService {

    final private PeerRepository peerRepository;

    final private UserRepository userRepository;

    final private TrackerRepository trackerRepository;

    final private WebSocketController webSocketController;

    final private PeerTrackerRepository peerTrackerRepository;

    final private TrackerResponseModelMapper trackerResponseModelMapper;

    @Override
    public void joinTracker(HttpServletRequest request, List<TrackerResponse> trackers) {
        String userAgent = request.getHeader("User-Agent");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            Peer peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                    .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            if (peer.getPeerTrackers() == null) {
                peer.setPeerTrackers(new ArrayList<>());
            }
            List<Tracker> trackers1 = trackerRepository.findByIdIn(trackers.stream().map(it -> {
                    return it.getId();
            }).collect(Collectors.toList()));
            List<TrackerResponse> tks = trackers.stream().map(it -> {
                if (!peer.getPeerTrackers().stream().map(item -> item.getId().getTracker().getId())
                        .collect(Collectors.toList()).contains(it.getId())) {
                    return it;
                }
                return null;
            }).collect(Collectors.toList());
            peer.getPeerTrackers().addAll(trackerRepository.findByIdIn(tks.stream().map(it -> {
                if (it != null) {
                    String log = FileOrFolderServiceImpl.logger(user.getUsername() + ": " + userAgent, null, 0,
                            "Joined", null, null, null);
                    try {
                        webSocketController.sendMessageTrackerToClients(log, it.getPort());
                    } catch (MessagingException | JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return it.getId();
                }
                return null;
            }).collect(Collectors.toList())).stream()
                    .map(it -> {
                        if (it != null) {
                            return PeerTracker.builder().id(PeerTrackerId.builder().peer(peer).tracker(it).build())
                                    .build();
                        }
                        return null;
                    })
                    .collect(Collectors.toList()).stream().filter(it -> it != null).collect(Collectors.toList()));
                    for (Tracker tracker : trackers1) {
                        if (tracker.getPeerTrackers().stream().filter(it -> it.getId().getPeer().getId().getUser().getUsername()
                                .equals(peer.getId().getUser().getUsername())
                                && it.getId().getPeer().getId().getUserAgent().equals(peer.getId().getUserAgent()))
                                .collect(Collectors.toList()).isEmpty()) {
                            for (PeerTorrent peerTorrent : peer.getPeerTorrents()) {
                                FileOrFolder lastFile = peerTorrent.getId().getTorrent().getFileOrFolders().getLast();
                                if (lastFile.getType().equals("FILE")) {
                                    for (FileOrFolder file : peerTorrent.getId().getTorrent().getFileOrFolders()) {
                                        String log = FileOrFolderServiceImpl.logger(
                                                peer.getId().getUser().getUsername() + ": "
                                                        + peer.getId().getUserAgent(),
                                                file, Double.parseDouble(String.valueOf(file.getLength())) / 1024, "Sharing",
                                                null, null, peerTorrent.getTypeRole());
                                        try {
                                            webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                                        } catch (MessagingException | JsonProcessingException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    String log = FileOrFolderServiceImpl.logger(
                                            peer.getId().getUser().getUsername() + ": "
                                                    + peer.getId().getUserAgent(),
                                            lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024,
                                            "Sharing", null, null, peerTorrent.getTypeRole());
                                    try {
                                        webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                                    } catch (MessagingException | JsonProcessingException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
            peerRepository.save(peer);
            return;
        }
        throw new LoginFailedException();
    }

    @Override
    public void disconnectTracker(HttpServletRequest request, TrackerResponse tracker) {
        String userAgent = request.getHeader("User-Agent");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            Peer peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                    .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            if (peer.getPeerTrackers() == null) {
                peer.setPeerTrackers(new ArrayList<>());
            }
            peerTrackerRepository.deleteById(PeerTrackerId.builder()
                    .tracker(trackerRepository.findById(tracker.getId()).get()).peer(peer).build());
            String log = FileOrFolderServiceImpl.logger(user.getUsername() + ": " + userAgent, null, 0, "Disconnected", null, null, null);
            try{
            webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
        } catch (MessagingException | JsonProcessingException e) {
            e.printStackTrace();
        }
            return;
        }
        throw new LoginFailedException();
    }

    @Override
    public List<TrackerResponse> getJoinedTrackers(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            Peer peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                    .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            List<Tracker> trackers1 = peer.getPeerTrackers().stream().map(it -> it.getId().getTracker()).collect(Collectors.toList());
            return trackers1.stream().map(it -> trackerResponseModelMapper.fromTracker(it)).collect(Collectors.toList());
        }
        throw new LoginFailedException();
    }

    @Override
    public void printTrackersNotification(List<TrackerResponse> trackers){
        List<Long> trackersId = trackers.stream().map(it -> it.getId()).collect(Collectors.toList());
        List<Tracker> trackers1 = trackerRepository.findByIdIn(trackersId);
        for (Tracker tracker : trackers1) {
            for(Peer p : tracker.getPeerTrackers().stream().map(it -> it.getId().getPeer()).collect(Collectors.toList())){
                for (PeerTorrent peerTorrent : p.getPeerTorrents()) {
                    FileOrFolder lastFile = peerTorrent.getId().getTorrent().getFileOrFolders().getLast();
                    if (lastFile.getType().equals("FILE")) {
                        for (FileOrFolder file : peerTorrent.getId().getTorrent().getFileOrFolders()) {
                            String log = FileOrFolderServiceImpl.logger(
                                    p.getId().getUser().getUsername() + ": "
                                            + p.getId().getUserAgent(),
                                    file, Double.parseDouble(String.valueOf(file.getLength())) / 1024, "Sharing",
                                    null, null, peerTorrent.getTypeRole());
                            try {
                                webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                            } catch (MessagingException | JsonProcessingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else {
                        String log = FileOrFolderServiceImpl.logger(
                                p.getId().getUser().getUsername() + ": "
                                        + p.getId().getUserAgent(),
                                lastFile, Double.parseDouble(String.valueOf(lastFile.getLength())) / 1024,
                                "Sharing", null, null, peerTorrent.getTypeRole());
                        try {
                            webSocketController.sendMessageTrackerToClients(log, tracker.getPort());
                        } catch (MessagingException | JsonProcessingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void getId() {
        // TODO
    }
}