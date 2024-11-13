package com.mmt.btl.service.impl;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.mmt.btl.entity.Peer;
import com.mmt.btl.entity.User;
import com.mmt.btl.entity.id.PeerId;
import com.mmt.btl.exception.LoginFailedException;
import com.mmt.btl.modelmapper.RegisterRequestModelMapper;
import com.mmt.btl.modelmapper.UserModelMapper;
import com.mmt.btl.repository.PeerRepository;
import com.mmt.btl.repository.UserRepository;
import com.mmt.btl.request.LoginRequest;
import com.mmt.btl.request.RegisterRequest;
import com.mmt.btl.response.LoginResponse;
import com.mmt.btl.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    final private UserRepository userRepository;

    final private UserModelMapper userModelMapper;

    final private PeerRepository peerRepository;

    final private RegisterRequestModelMapper registerRequestModelMapper;

    public LoginResponse login(HttpServletRequest servletRequest, LoginRequest request) throws LoginFailedException {
        if (request.getUsername() != null && !request.getUsername().equals("")) {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new LoginFailedException());
            if (user.getPassword().equals(request.getPassword())) {
                String userAgent = servletRequest.getHeader("User-Agent");
                Optional<Peer> peer = peerRepository.findById(PeerId.builder().user(user).userAgent(userAgent).build());
                if (peer.isEmpty()) {
                    peerRepository.save(Peer.builder().id(PeerId.builder().user(user).userAgent(userAgent).build())
                            .status(true).build());
                } else {
                    peer.get().setStatus(true);
                }
                return userModelMapper.toLoginResponse(user);
            }
        }
        throw new LoginFailedException();
    }

    public void register(RegisterRequest request) throws LoginFailedException {
        if (request.getUsername() != null && !request.getUsername().equals("")) {
            Optional<User> user = userRepository.findByUsername(request.getUsername());
            if (user.isEmpty()) {
                if (request.getPassword() != null && !request.getPassword().equals("")) {
                    userRepository.save(registerRequestModelMapper.toUser(request));
                    return;
                }
            }
            throw new LoginFailedException("User is existed...!");
        }
        throw new LoginFailedException("Request is invalid...!");
    }
}
