package com.example.users.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtInterface {

    private final String ISSUER = "ingesis.uniquindio.edu.co";
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(String email) {
        return buildToken(new HashMap<>(), email);
    }

    private String buildToken(Map<String, Object> extraClaims, String email) {

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }
    public boolean isTokenValid(String token) {
        final String issuer = extractIssuer(token);
        return (issuer.equals("ingesis.uniquindio.edu.co"));
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    public String extractIssuer(String token) {
        return extractAllClaims(token).getIssuer();
    }
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }
   
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}