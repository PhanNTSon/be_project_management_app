package pma.common.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Lớp Filter kích hoạt mỗi lần có Request đi vào server (OncePerRequestFilter).
 * Có nhiệm vụ chặn HTTP Request để trích xuất chữ ký JWT, kiểm tra tính hợp lệ,
 * và nạp thông tin quyền hạn của người dùng vào bối cảnh an ninh (Security Context) của Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;

    /**
     * Phương thức chính chạy logic lọc của Filter.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Lấy đoạn mã JWT đính kèm trong header 'Authorization'
        String authHeader = request.getHeader("Authorization");

        // 1. Kiểm tra header xem có chứa Bearer Token không
        // Nếu không có Token hoặc sai định dạng thì lờ qua Filter đoạn này, đi tiếp đến Filter khác.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Tách chuỗi token ra khỏi chuỗi "Bearer " (Bắt đầu từ kí tự thứ 7)
        String jwt = authHeader.substring(7);
        try {
            // Giải mã token để lấy bộ khung Claims (dữ liệu payload)
            Claims claims = jwtService.parseToken(jwt);

            // Bóc tách Username ra khỏi Claims
            String username = jwtService.extractUsername(claims);

            // 3. Nếu đọc được tên user nhưng trong Context chưa có thông tin bảo mật (tức chưa đăng nhập ở luồng này lúc trước)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Lấy chi tiết UserDetails định danh và quyền từ DB
                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

                // 4. Đối chiếu xem token còn hạn và đúng người hay không
                if (jwtService.validateToken(claims, userDetails.getUsername())) {

                    // Nếu Token hoàn toàn hợp lệ, tái tạo đối tượng Authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Nạp Authentication đó vào ContextHolder. 
                    // Bất kỳ đoạn logic nào phía sau cũng sẽ biết Request này đại diện cho "username" này.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            // Nuốt lỗi (token sai, hết hạn v.v...). Code không ném lỗi ra ngoài mà chỉ việc 
            // bỏ qua (Context vẫn null, người dùng sẽ tự bị văng ra nếu API đó bị chặn Authentication).
        }
        filterChain.doFilter(request, response);
    }
}
