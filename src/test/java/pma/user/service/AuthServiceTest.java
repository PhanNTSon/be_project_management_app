package pma.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pma.common.exception.CustomException.EmailAlreadyExistException;
import pma.common.exception.CustomException.ExpiredRefreshTokenException;
import pma.common.exception.CustomException.InvalidPasswordException;
import pma.common.exception.CustomException.InvalidRefreshTokenException;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.exception.CustomException.UsernameAlreadyExistException;
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

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private RoleRepo roleRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepo refreshTokenRepo;
    @Mock
    private UserRoleRepo userRoleRepo;

    @InjectMocks
    private AuthService authService;

    // ==========================================
    // CÁC TEST CASE CHO HÀM registerUser
    // ==========================================

    /**
     * Test case:
     * Khi username đã tồn tại trong hệ thống -> phải throw
     * UsernameAlreadyExistException
     */
    @Test
    void registerUser_UsernameAlreadyExists_ShouldThrowException() {

        RequestRegisterDto dto = new RequestRegisterDto("test@email.com", "username", "password");

        User existingUser = new User("exist@email.com", "username", "password123");

        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UsernameAlreadyExistException.class,
                () -> authService.registerUser(dto));

        verify(userRepo, never()).save(any(User.class));
    }

    /**
     * Test case:
     * Khi email đã tồn tại trong hệ thống -> phải throw EmailAlreadyExistException
     */
    @Test
    void registerUser_EmailAlreadyExists_ShouldThrowException() {

        RequestRegisterDto dto = new RequestRegisterDto("test@email.com", "username", "password");

        User existingUser = new User("test@email.com", "anotherUser", "password123");

        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.empty());

        when(userRepo.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                EmailAlreadyExistException.class,
                () -> authService.registerUser(dto));

        verify(userRepo, never()).save(any(User.class));
    }

    /**
     * Test case:
     * Khi dữ liệu hợp lệ -> user phải được tạo và lưu xuống DB, UserRole phải được lưu
     */
    @Test
    void registerUser_ValidData_ShouldSaveUserSuccessfully() {

        RequestRegisterDto dto = new RequestRegisterDto("newuser", "passwod", "new@email.com");

        Role userRole = new Role("USER", "");

        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.empty());

        when(userRepo.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        when(roleRepo.findByRoleName("USER"))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encodedPassword");

        authService.registerUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepo).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("new@email.com", savedUser.getEmail());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPasswordHash());

        // Verify that UserRole was saved with the user and role
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepo, times(1)).save(userRoleCaptor.capture());

        UserRole savedUserRole = userRoleCaptor.getValue();
        assertEquals(userRole, savedUserRole.getRole());
    }

    // ==========================================
    // CÁC TEST CASE CHO HÀM loginUser
    // ==========================================

    /**
     * Test case:
     * Username không tồn tại -> phải throw UserNotFoundException
     */
    @Test
    void loginUser_UserNotFound_ShouldThrowException() {
        // Arrange
        RequestLoginDto dto = new RequestLoginDto("unknown_user", "password");
        when(userRepo.findByUsername(dto.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            authService.loginUser(dto);
        });
    }

    /**
     * Test case:
     * Password sai -> phải throw InvalidPasswordException
     */
    @Test
    void loginUser_InvalidPassword_ShouldThrowException() {
        // Arrange
        RequestLoginDto dto = new RequestLoginDto("user", "wrong_password");

        User mockUser = new User("mockEmail@gmail.com", "user", "encoded_correct_password");

        when(userRepo.findByUsername(dto.getUsername())).thenReturn(Optional.of(mockUser));

        // Giả lập password encoder trả về false vì sai pass
        when(passwordEncoder.matches(dto.getPassword(), mockUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> {
            authService.loginUser(dto);
        });
    }

    /**
     * Test case:
     * Login thành công -> phải tạo access token + refresh token
     * và lưu refresh token vào database
     */
    @Test
    void loginUser_ValidCredentials_ShouldReturnLoginResult() {
        // Arrange
        RequestLoginDto dto = new RequestLoginDto("user", "correct_password");

        User mockUser = new User("mockEmail@gmail.com", "user", "encoded_correct_password");

        // Dạy cho các Mock Object cách phản hồi
        when(userRepo.findByUsername(dto.getUsername())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(dto.getPassword(), mockUser.getPasswordHash())).thenReturn(true);

        when(jwtService.generateToken("user")).thenReturn("mock_access_token");
        when(refreshTokenService.generateRandomToken()).thenReturn("mock_raw_refresh_token");
        when(refreshTokenService.hashToken("mock_raw_refresh_token")).thenReturn("mock_hashed_refresh_token");

        // Act
        LoginResultDto result = authService.loginUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals("mock_access_token", result.getAccessToken()); // Điều chỉnh getter theo thiết kế LoginResult
        assertEquals("mock_raw_refresh_token", result.getRefreshToken());

        // Xác minh RefreshToken đã được tạo và lưu xuống DB thành công
        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepo, times(1)).save(rtCaptor.capture());

        RefreshToken savedRt = rtCaptor.getValue();
        assertEquals(mockUser, savedRt.getUser());
        assertEquals("mock_hashed_refresh_token", savedRt.getTokenHash());
        assertNotNull(savedRt.getExpiryDate());
    }

    // ==========================================
    // CÁC TEST CASE CHO HÀM logoutUser
    // ==========================================

    @Test
    void logoutUser_TokenNotFound_ShouldThrowException() {

        // Arrange
        String refreshToken = "invalid";
        String tokenHash = "hashedInvalid";

        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.logoutUser(refreshToken));

        verify(refreshTokenRepo, never()).delete(any());
    }

    @Test
    void logoutUser_ExpiredToken_ShouldDeleteAndThrow() {

        // Arrange
        String refreshToken = "expired";
        String tokenHash = "hashedExpired";

        User user = new User(
                "test@email.com",
                "testuser",
                "password123");

        RefreshToken token = spy(new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().plusDays(1)));

        doReturn(true).when(token).isExpired();

        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        // Act & Assert
        assertThrows(
                ExpiredRefreshTokenException.class,
                () -> authService.logoutUser(refreshToken));

        // Verify expired token was deleted
        verify(refreshTokenRepo).delete(token);
    }

    @Test
    void logoutUser_ValidToken_ShouldDeleteSuccessfully() {

        // Arrange
        String refreshToken = "validRefreshToken";
        String tokenHash = "hashedRefreshToken";

        User user = new User(
                "test@email.com",
                "testuser",
                "password123");

        RefreshToken storedRefreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().plusDays(7)); // Token vẫn còn hạn

        // ✅ Mock: hash của refresh token
        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        // ✅ Mock: tìm refresh token trong DB
        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(storedRefreshToken));

        // Act
        authService.logoutUser(refreshToken);

        // Assert
        // ✅ Token phải được xóa từ DB
        verify(refreshTokenRepo).delete(storedRefreshToken);
    }

    // ==========================================
    // CÁC TEST CASE CHO HÀM refreshAccessToken
    // ==========================================
    @Test
    void refreshAccessToken_TokenNotFound_ShouldThrowException() {

        // Arrange
        String refreshToken = "invalid";
        String tokenHash = "hashedInvalid";

        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refreshAccessToken(refreshToken));

        verify(refreshTokenRepo, never()).save(any());
    }

    @Test
    void refreshAccessToken_ExpiredToken_ShouldDeleteAndThrow() {

        String refreshToken = "expired";
        String tokenHash = "hashedExpired";

        User user = new User(
                "test@email.com",
                "testuser",
                "password123");

        RefreshToken token = spy(new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().plusDays(1)));

        doReturn(true).when(token).isExpired();

        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        assertThrows(
                ExpiredRefreshTokenException.class,
                () -> authService.refreshAccessToken(refreshToken));

        verify(refreshTokenRepo).delete(token);
    }

    @Test
    void refreshAccessToken_ValidToken_ShouldReturnNewAccessTokenOnly() {

        // Arrange
        String refreshToken = "validRefreshToken";
        String tokenHash = "hashedRefreshToken";
        String newAccessToken = "newAccessTokenXYZ";

        User user = new User(
                "test@email.com",
                "testuser",
                "password123");

        RefreshToken storedRefreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().plusDays(7)); // Token vẫn còn hạn

        // ✅ Mock: hash của refresh token
        when(refreshTokenService.hashToken(refreshToken))
                .thenReturn(tokenHash);

        // ✅ Mock: tìm refresh token trong DB
        when(refreshTokenRepo.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(storedRefreshToken));

        // ✅ Mock: generate new access token
        when(jwtService.generateToken(user.getUsername()))
                .thenReturn(newAccessToken);

        // Act
        RefreshResultDto result = authService.refreshAccessToken(refreshToken);

        // Assert
        // ✅ Chỉ trả về access token mới, refresh token vẫn cũ
        assertNotNull(result);
        assertEquals(newAccessToken, result.getNewAccessToken());

        // ✅ KHÔNG tạo refresh token mới
        verify(refreshTokenService, never()).generateRandomToken();

        // ✅ KHÔNG update database
        verify(refreshTokenRepo, never()).save(any());
    }
}
