package pma.common.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Lớp cấu hình an ninh tổng thể của toàn bộ ứng dụng Spring Boot.
 * Thiết lập CORS, CSRF, tắt Session, quy định phân quyền endpoint, và kẹp các Filter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    /**
     * Cấu hình luồng thực thi các chốt chặn Security (SecurityFilterChain).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Kích hoạt chặn CORS dựa trên hàm corsConfigurationSource bên dưới
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì ứng dụng này sử dụng kiến trúc Token (JWT), không dùng Cookie-Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Chuyển sang STATELESS (Không lưu phiên đăng nhập cục bộ trên RAM phía server)
                .authorizeHttpRequests(auth -> auth
                        // Cấu hình danh sách endpoint và quyền tương ứng
                        .requestMatchers("/api/auth/**").permitAll() // Các API liên quan tới đăng nhập/đăng ký thì ai cũng vào được
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Các API bắt đầu bằng /admin/ bắt buộc phải có Role ADMIN
                        .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN") // Bắt buộc Role USER hoặc ADMIN
                        .anyRequest().authenticated()) // Tất cả API khác đều phải có token đăng nhập hợp lệ
                
                // Đẩy Filter tự chế (JwtFilter) lên trước Filter kiểm tra User/Password mặc định của Spring.
                // Điều này có nghĩa: Cứ có Request tới là mình tự kiểm tra Token trước!
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                
                // Tắt các form đăng nhập rườm rà mặc định do không dùng kiến trúc Monolith MVC
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }

    /**
     * Chỉ định cơ chế mã hóa mật khẩu chung cho toàn ứng dụng.
     * BCrypt là thuật toán phổ biến, một hàm Hash một chiều khó bẻ khóa.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Khai báo chính sách CORS (Dùng chung cho cả Restful).
     * Cho phép các domain nào được gửi request tới Backend này.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Danh sách các cổng Frontend được whitelist (Ví dụ ứng dụng React/Vite local chạy gốc 5173)
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Cho phép đính kèm credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Áp dụng Cấu hình này cho toàn bộ endpoints /**
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
