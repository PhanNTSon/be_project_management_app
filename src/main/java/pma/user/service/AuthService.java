package pma.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import pma.common.exception.CustomException.EmailAlreadyExistException;
import pma.common.exception.CustomException.ExpiredRefreshTokenException;
import pma.common.exception.CustomException.InvalidPasswordException;
import pma.common.exception.CustomException.InvalidRefreshTokenException;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.exception.CustomException.UsernameAlreadyExistException;
import pma.common.exception.CustomException.RoleNotFoundException;
import pma.common.security.JwtService;
import pma.user.dto.LoginResultDto;
import pma.user.dto.RefreshResultDto;
import pma.user.dto.request.RequestLoginDto;
import pma.user.dto.request.RequestRegisterDto;
import pma.user.entity.RefreshToken;
import pma.user.entity.Role;
import pma.user.entity.User;
import pma.user.entity.UserRole;
import pma.user.repository.RefreshTokenRepo;
import pma.user.repository.RoleRepo;
import pma.user.repository.UserRepo;
import pma.user.repository.UserRoleRepo;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRoleRepo userRoleRepo;

    /**
     * Xử lý logic đăng ký người dùng mới.
     * Lần lượt kiểm tra xem username hoặc email đã tồn tại trong hệ thống chưa.
     * Nếu chưa, tạo một thực thể User mới, mã hóa mật khẩu, gán quyền mặc định là
     * USER và lưu vào database.
     */
    @Transactional
    public void registerUser(RequestRegisterDto registerDto) {

        // Kiểm tra xem username đã tồn tại trong DB chưa, nếu có ném ngoại lệ
        if (userRepo.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistException();
        }

        // Kiểm tra xem email đã tồn tại trong DB chưa, nếu có ném ngoại lệ
        if (userRepo.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException();
        }

        // Lấy Role mặc định là USER từ DB
        Role userRole = roleRepo.findByRoleName("USER").orElseThrow(() -> new RoleNotFoundException("USER"));

        // Khởi tạo đối tượng User mới và mã hóa mật khẩu
        User newUser = new User(registerDto.getEmail(),
                registerDto.getUsername(),
                passwordEncoder.encode(registerDto.getPassword()));

        // Lưu thông tin người dùng mới vào hệ thống trước (để generate userId)
        userRepo.save(newUser);

        // Tạo và lưu UserRole sau khi User đã được persist (có userId)
        UserRole newUserRole = new UserRole(newUser, userRole);
        userRoleRepo.save(newUserRole);
    }

    /**
     * Xử lý đăng nhập của người dùng.
     * Kiểm tra username và so sánh mật khẩu. Nếu thông tin đúng, sẽ tạo ra Access
     * Token (JWT)
     * và Refresh Token chữ ký ngẫu nhiên (raw token), lưu Refresh Token dạng hash
     * vào CSDL,
     * rồi trả về DTO chứa thông tin user cùng các token.
     */
    @Transactional
    public LoginResultDto loginUser(RequestLoginDto loginDto) {
        // Tìm người dùng trong CSDL bằng username, nếu không có thì ném ngoại lệ
        User user = userRepo.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException());

        // Xác thực mật khẩu đang nhập với mật khẩu đã mã hóa được lưu
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        // Chế tạo Access Token (JWT) mang thông tin username
        String accessToken = jwtService.generateToken(user.getUsername());

        // Chế tạo chuỗi Refresh Token ngẫu nhiên để gửi về client
        String rawToken = refreshTokenService.generateRandomToken();

        // Băm Refresh Token để lưu database, tăng độ bảo mật nếu DB bị rò rỉ
        String refreshTokenHash = refreshTokenService.hashToken(rawToken);

        // Tạo bản ghi RefreshToken mới với thời hạn hiệu lực 7 ngày và lưu vào DB
        RefreshToken rt = new RefreshToken(user, refreshTokenHash, LocalDateTime.now().plusDays(7));
        refreshTokenRepo.save(rt);

        // Trả về kết quả hiển thị cho client (bao gồm thông tin user cơ bản và token
        // raw)
        return new LoginResultDto(user, accessToken, rawToken);
    }

    /**
     * Xử lý đăng xuất thông qua Refresh Token đang dùng.
     * Việc xóa token khỏi CSDL sẽ vô hiệu hóa chuỗi cấp phát Access Token mới.
     */
    @Transactional
    public void logoutUser(String refreshToken) {
        // Xóa cứng Refresh Token khỏi DB dựa trên đoạn hash của token
        refreshTokenRepo.deleteByTokenHash(refreshToken);
    }

    /**
     * Cấp phát lại Access Token từ Refresh Token cũ (không thay đổi Refresh Token).
     * Chỉ sinh Access Token mới, Refresh Token vẫn giữ nguyên.
     */
    @Transactional
    public RefreshResultDto refreshAccessToken(String refreshToken) {

        // Băm khóa token thô lấy từ client để truy vấn DB
        String tokenHash = refreshTokenService.hashToken(refreshToken);

        // Lấy bản ghi tồn tại trong hệ thống, ném lỗi token không hợp lệ nếu không có
        RefreshToken rt = refreshTokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException());

        // Kiểm tra Refresh Token hết hạn chưa. Nếu đã hạn, thu hồi từ DB và ném lỗi.
        if (rt.isExpired()) {
            refreshTokenRepo.delete(rt);
            throw new ExpiredRefreshTokenException();
        }

        // Tạo lại Access Token mới (Refresh Token không đổi)
        String newAccessToken = jwtService.generateToken(rt.getUser().getUsername());

        // Gửi Access Token mới trả về phía client (Refresh Token vẫn giữ nguyên)
        return new RefreshResultDto(newAccessToken);
    }

}
