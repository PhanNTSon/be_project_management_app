package pma.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pma.user.entity.Role;
import pma.user.entity.User;
import pma.user.entity.UserRole;
import pma.user.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    void loadUserByUsername_UserFound_ShouldReturnUserDetails() {
        // 1. Arrange (Chuẩn bị dữ liệu)
        String username = "testuser";

        Role role = new Role("ROLE_USER", "Regular user role");

        User mockUser = new User("a@gmail.com", "testUser", "encodedPassword", "Test User");
        mockUser.assignRole(role);

        // Dạy cho mockRepo biết phải trả về cái gì khi được gọi [6]
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // 2. Act (Thực thi)
        UserDetails result = customUserDetailService.loadUserByUsername(username);

        // 3. Assert (Kiểm tra kết quả)
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        // Xác minh repo đã thực sự được gọi 1 lần [6]
        verify(userRepo, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserNotFound_ShouldThrowException() {
        // 1. Arrange
        String username = "unknown_user";
        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        // 2 & 3. Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername(username);
        });

        verify(userRepo, times(1)).findByUsername(username);
    }
}
