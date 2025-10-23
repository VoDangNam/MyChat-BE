package com.example.My_Chat.Config;

import com.example.My_Chat.model.User;
import com.fasterxml.jackson.core.JacksonException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    private final long EXPIRATION_TIME = Duration.ofHours(1).toMillis();

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getUsername())  //username vao
                .claim("id",user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date()) //thoi gian tao ra
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public Claims  extractAllClaims(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }catch (ExpiredJwtException e){
            System.out.println("token het han");
            throw e;
        } catch (JwtException e){
            System.out.println("token ko hop le");
            throw  e;
        }
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token){
        return extractAllClaims(token).get("id",Long.class);
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role",String.class);
    }

    public boolean isTokenExpired(String token){
        Date expired = extractAllClaims(token).getExpiration();
        return expired.before(new Date());
    }

    public boolean validateToken(String token, User user){
        final String username =extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

}
