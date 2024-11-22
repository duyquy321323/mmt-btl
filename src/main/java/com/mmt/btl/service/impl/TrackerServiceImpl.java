package com.mmt.btl.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.User;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.exception.LoginFailedException;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.modelmapper.TrackerResponseModelMapper;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.TrackerRepository;
import com.mmt.btl.repository.UserRepository;
import com.mmt.btl.response.TrackerResponse;
import com.mmt.btl.service.TrackerService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class TrackerServiceImpl implements TrackerService {

    final private TrackerRepository trackerRepository;

    final private TrackerResponseModelMapper trackerResponseModelMapper;

    final private UserRepository userRepository;

    final private PeerRepository peerRepository;

    @Override
    public List<TrackerResponse> getAllTracker() {
        return trackerRepository.findAll().stream().map(it -> trackerResponseModelMapper.fromTracker(it))
                .collect(Collectors.toList());
    }

    @Override
    public List<TrackerResponse> getMyTrackers(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userDetails = (UserDetails) auth.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new MMTNotFoundException());
            Peer peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build())
                    .orElseThrow(() -> new MMTNotFoundException("Peer Not Found...!"));
            return peer.getPeerTrackers().stream()
                    .map(item -> trackerResponseModelMapper.fromTracker(item.getId().getTracker()))
                    .collect(Collectors.toList());
        }
        throw new LoginFailedException("Please Login Before Request...!");
    }
}