package pma.common.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }

    // extract username from token
    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    // extract expiration
    public Date extractExpiration(Claims claims) {
        return claims.getExpiration();
    }

    // 2. Sử dụng Builder Pattern, verifyWith() và getPayload()
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // check token expired
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    // 3. Sử dụng signWith(SecretKey) thay vì signWith(Enum, String)
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username) // Builder mới hỗ trợ hàm subject() thay cho setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(getSigningKey()) // Thuật toán HS256 sẽ tự động được chọn dựa trên SecretKey
                .compact();
    }

    // validate token
    public boolean validateToken(Claims claims, String username) {
        return claims.getSubject().equals(username)
                && !isTokenExpired(claims);
    }
}
