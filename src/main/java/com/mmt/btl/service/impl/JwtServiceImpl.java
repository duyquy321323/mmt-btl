package com.mmt.btl.service.impl;

import java.security.InvalidParameterException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mmt.btl.entity.User;
import com.mmt.btl.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;


@SuppressWarnings("deprecation")
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public String generateToken(User user) {
        // properties -> claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        try {
            String token = Jwts.builder().setClaims(claims)
                    .setSubject(user.getUsername())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256) // mã hóa chữ ký bằng thuật toán HS256
                    .compact();
            return token;
        } catch (Exception e) {
            throw new InvalidParameterException("Cannot create jwt token, error: " + e.getMessage());
        }
    }

    @SneakyThrows
    private Claims extractAllClaims(String token) {
        // có check validate token luôn rồi
        return Jwts.parser().setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token); 
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractUsername(String token) {
        // hàm lấy username từ token
        return extractClaim(token, Claims::getSubject); // lấy Sub của payload token
    }

    private Key getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(secret); // giải mã chữ kí secret đã cài sẵn và chuyển về byte
        return Keys.hmacShaKeyFor(bytes); // tạo khóa secret key HMAC từ chữ kí dạng byte
    }

    private boolean isTokenExpired(String token) {
        // hàm kiểm tra token đã quá hạn chưa
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date()); // nếu hạn token hiện tại mà quá hạn thì trả về true
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token); // lấy username từ token
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public Date extractExpirationToken(String token) {
        return Jwts.parser().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getPayload().getExpiration();
    }
}