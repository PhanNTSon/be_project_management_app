package pma.common.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pma.user.entity.User;
import pma.user.repository.UserRepo;

/**
 * Lớp dịch vụ lấy thông tin định danh của người dùng từ Database và chuyển đổi
 * sang chuẩn UserDetails do Spring Security yêu cầu.
 */
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepo userRepository;

    public CustomUserDetailService(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Hàm bắt buộc phải implement từ UserDetailsService.
     * Nó được Spring Security gọi ẩn phía dưới khi cần xác thực hoặc lấy quyền.
     *
     * @Transactional để giữ session mở, cho phép lazy-load UserRoles từ User entity.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {

        // Bước 1: Tìm dưới Database bằng username từ entity User của project
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Bước 2: Chuyển đổi danh sách role của entity sang danh sách SimpleGrantedAuthority của Spring
        // @Transactional ensures UserRoles are loaded (LAZY loading)
        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(ur -> new SimpleGrantedAuthority(ur.getRole().getRoleName()))
                .toList();

        // Bước 3: Trả về đối tượng User của Spring Security (KHÔNG phải User entity của project)
        // Chứa username, password (hash) và các quyền, làm cơ sở để Spring kiểm tra phân quyền.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities);
    }
}
