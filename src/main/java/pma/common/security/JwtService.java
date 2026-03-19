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

/**
 * Lớp Service chịu trách nhiệm xử lý toàn bộ vòng đời của JSON Web Token (JWT).
 * Bao gồm việc tạo mã Token, trích xuất thông tin, và xác thực tính vẹn toàn của mã.
 */
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Đối tượng Khóa bí mật dùng để ký và giải mã JWT (Theo chuẩn HMAC-SHA)
    private SecretKey signingKey;

    /**
     * Khởi tạo SecretKey từ cấu hình chuỗi Base64 ngay sau khi Bean được Spring Boot tạo ra.
     */
    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getSigningKey() {
        return signingKey;
    }

    /**
     * Trích xuất thông tin người dùng (Subject thường lưu Username) từ bộ nhận diện Claims.
     */
    // extract username from token
    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Trích xuất thời điểm hết hạn của token từ bộ nhận diện Claims.
     */
    // extract expiration
    public Date extractExpiration(Claims claims) {
        return claims.getExpiration();
    }

    /**
     * Giải mã một chuỗi JWT String ra thành dữ liệu Claims.
     * Sử dụng Khóa bí mật (signingKey) để xác minh token này thực sự do Server ký sinh ra.
     * Sẽ ném ngoại lệ nếu Token đã bị giả mạo thay đổi nội dung.
     */
    // 2. Sử dụng Builder Pattern, verifyWith() và getPayload()
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra xem Token đã quá hạn thời gian tồn tại hay chưa.
     */
    // check token expired
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Tạo một chuỗi Token JWT mới gắn liền với tên đăng nhập của người dùng.
     * Thiết lập thời hạn tồn tại (ví dụ: 1 ngày) và ký bảo mật bằng thuật toán HS256.
     */
    // 3. Sử dụng signWith(SecretKey) thay vì signWith(Enum, String)
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username) // Bộ dữ liệu định danh người dùng mặc định
                .issuedAt(new Date(System.currentTimeMillis())) // Thời điểm cấp phát
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Thời điểm hết hạn
                .signWith(getSigningKey()) // Thuật toán HS256 sẽ tự động được chọn dựa trên SecretKey
                .compact(); // Đóng gói thành chuỗi String an toàn mang gửi qua đường truyền web
    }

    /**
     * Kiểm tra tính hợp lệ toàn diện của Token:
     * - Token phải chưa hết hạn.
     * - Username từ token phải khớp với User đang được Security truy xuất.
     */
    // validate token
    public boolean validateToken(Claims claims, String username) {
        return claims.getSubject().equals(username)
                && !isTokenExpired(claims);
    }
}
