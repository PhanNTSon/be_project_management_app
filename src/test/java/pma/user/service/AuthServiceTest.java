package pma.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pma.common.exception.CustomException.EmailAlreadyExistException;
import pma.common.exception.CustomException.InvalidPasswordException;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.exception.CustomException.UsernameAlreadyExistException;
import pma.common.security.JwtService;
import pma.user.dto.LoginResult;
import pma.user.dto.request.RequestLoginDto;
import pma.user.dto.request.RequestRegisterDto;
import pma.user.entity.RefreshToken;
import pma.user.entity.User;
import pma.user.repository.RefreshTokenRepo;
import pma.user.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private RefreshTokenRepo refreshTokenRepo;

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

        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class, () -> {
            authService.registerUser(dto);
        });

        verify(userRepo, never()).save(any(User.class));
    }

    /**
     * Test case:
     * Khi email đã tồn tại trong hệ thống -> phải throw EmailAlreadyExistException
     */
    @Test
    void registerUser_EmailAlreadyExists_ShouldThrowException() {

        // ---------- Arrange ----------
        RequestRegisterDto dto = new RequestRegisterDto("test@email.com", "username", "password");

        // username chưa tồn tại
        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.empty());

        // email đã tồn tại
        when(userRepo.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new User()));

        // ---------- Act & Assert ----------
        assertThrows(EmailAlreadyExistException.class, () -> {
            authService.registerUser(dto);
        });

        verify(userRepo, never()).save(any(User.class));
    }

    /**
     * Test case:
     * Khi dữ liệu hợp lệ -> user phải được tạo và lưu xuống DB
     */
    @Test
    void registerUser_ValidData_ShouldSaveUserSuccessfully() {

        // ---------- Arrange ----------
        RequestRegisterDto dto = new RequestRegisterDto("new@email.com", "newuser", "password");

        when(userRepo.findByUsername(dto.getUsername()))
                .thenReturn(Optional.empty());

        when(userRepo.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("encodedPassword");

        // ---------- Act ----------
        authService.registerUser(dto);

        // ---------- Assert ----------
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepo, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("new@email.com", savedUser.getEmail());
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
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
        LoginResult result = authService.loginUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals("mock_access_token", result.getAccessToken()); // Điều chỉnh getter theo thiết kế LoginResult
        assertEquals("mock_hashed_refresh_token", result.getRefreshToken());

        // Xác minh RefreshToken đã được tạo và lưu xuống DB thành công
        ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepo, times(1)).save(rtCaptor.capture());

        RefreshToken savedRt = rtCaptor.getValue();
        assertEquals(mockUser, savedRt.getUser());
        assertEquals("mock_hashed_refresh_token", savedRt.getTokenHash());
        assertNotNull(savedRt.getExpiryDate());
    }
}
