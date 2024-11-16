package com.mmt.btl.service;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;

import com.mmt.btl.entity.User;

public interface JwtService {
    public String generateToken(User user);

    public String extractUsername(String token);

    public boolean validateToken(String token, UserDetails userDetails);

    public Date extractExpirationToken(String token);
}