package pma.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Dùng ReflectionTestUtils để nạp giá trị vào biến private có gắn @Value [9]
        // Chuỗi Secret phải là chuỗi Base64 hợp lệ độ dài đủ 256 bits trở lên
        String validBase64Secret = "YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleS1tdXN0LWJlLWF0LWxlYXN0LTMyLWJ5dGVz";
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", validBase64Secret);
    }

    @Test
    void generateToken_And_ExtractUsername_ShouldWorkCorrectly() {
        // Arrange
        String username = "admin";

        // Act: Sinh token
        String token = jwtService.generateToken(username);

        // Assert: Trích xuất ngược lại username và so sánh
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_WithValidTokenAndMatchingUser_ShouldReturnTrue() {
        // Arrange
        String username = "admin";
        String token = jwtService.generateToken(username);

        // Act & Assert
        assertTrue(jwtService.validateToken(token, username));
    }

    @Test
    void validateToken_WithUnmatchingUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken("admin");

        // Act & Assert
        // Cố tình validate bằng một username khác
        assertFalse(jwtService.validateToken(token, "wrong_user"));
    }
}
