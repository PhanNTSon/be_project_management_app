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

    // 1. Sinh ra một Random String (Opaque Token) an toàn 256-bit
    public String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {

        return DigestUtils.md5DigestAsHex(
                rawToken.getBytes(StandardCharsets.UTF_8));
    }
}
