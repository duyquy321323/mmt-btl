package com.mmt.btl.security;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.exception.LoginFailedException;
import com.mmt.btl.exception.MMTNotFoundException;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.UserRepository;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Primary
public class CustomHandlerLogout implements LogoutHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PeerRepository peerRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            user = (UserDetails) auth.getPrincipal();
        }
        if (user != null) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                Optional<Peer> peer = peerRepository.findById(PeerId.builder()
                        .user(userRepository.findByUsername(user.getUsername())
                                .orElseThrow(() -> new MMTNotFoundException()))
                        .userAgent(userAgent)
                        .build());
                if (peer.isPresent()) {
                    Peer newPeer = peer.get();
                    newPeer.setStatus(false);
                    peerRepository.save(newPeer);
                    return;
                }
            }
        }
        throw new LoginFailedException("Logout failed...!");
    }
}