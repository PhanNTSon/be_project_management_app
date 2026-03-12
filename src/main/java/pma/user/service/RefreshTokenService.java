package pma.user.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RefreshTokenService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    /**
     * Sinh ra một Token ngẫu nhiên (Opaque Token) sử dụng SecureRandom 256-bit an toàn.
     * Chuỗi này đóng vai trò làm Refresh Token dạng thô (raw token) cấp phát cho client.
     */
    // 1. Sinh ra một Random String (Opaque Token) an toàn 256-bit
    public String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Mã hóa (Băm) chuỗi Refresh Token ngẫu nhiên thành một chuỗi Hex md5.
     * Mục đích là để lưu trữ an toàn mã băm vào Database. Nếu DB bị hacker đánh cắp, 
     * hacker cũng không thể sử dụng token băm này để lấy Access Token do không có token raw.
     */
    public String hashToken(String rawToken) {
        // Chuyển rawToken thành mảng byte chuẩn UTF-8 và băm MD5 ra chuỗi Hex
        return DigestUtils.md5DigestAsHex(
                rawToken.getBytes(StandardCharsets.UTF_8));
    }
}
