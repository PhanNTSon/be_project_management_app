package pma.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Dùng ReflectionTestUtils để nạp giá trị vào biến private có gắn @Value [9]
        // Chuỗi Secret phải là chuỗi Base64 hợp lệ độ dài đủ 256 bits trở lên
        String validBase64Secret = "YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleS1tdXN0LWJlLWF0LWxlYXN0LTMyLWJ5dGVz";
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", validBase64Secret);
        jwtService.init();
    }

    @Test
    void generateToken_ShouldReturnValidJwtString() {

        String username = "admin";

        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_ValidToken_ShouldReturnClaims() {

        String username = "admin";

        String token = jwtService.generateToken(username);

        Claims claims = jwtService.parseToken(token);

        assertEquals(username, claims.getSubject());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {

        String username = "admin";

        String token = jwtService.generateToken(username);

        Claims claims = jwtService.parseToken(token);

        String extracted = jwtService.extractUsername(claims);

        assertEquals(username, extracted);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {

        String username = "admin";

        String token = jwtService.generateToken(username);

        Claims claims = jwtService.parseToken(token);

        Date expiration = jwtService.extractExpiration(claims);

        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_ValidTokenAndMatchingUser_ShouldReturnTrue() {

        String username = "admin";

        String token = jwtService.generateToken(username);

        Claims claims = jwtService.parseToken(token);

        boolean result = jwtService.validateToken(claims, username);

        assertTrue(result);
    }

    @Test
    void validateToken_WithDifferentUser_ShouldReturnFalse() {

        String token = jwtService.generateToken("admin");

        Claims claims = jwtService.parseToken(token);

        boolean result = jwtService.validateToken(claims, "user2");

        assertFalse(result);
    }

    @Test
    void parseToken_WithExpiredToken_ShouldThrowException() {

        String username = "admin";

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(jwtService.getSigningKey())
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.parseToken(token));
    }
}
