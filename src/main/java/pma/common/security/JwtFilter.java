package pma.common.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import pma.common.exception.ErrorResponse;

/**
 * Lớp Filter kích hoạt mỗi lần có Request đi vào server (OncePerRequestFilter).
 * Có nhiệm vụ chặn HTTP Request để trích xuất chữ ký JWT, kiểm tra tính hợp lệ,
 * và nạp thông tin quyền hạn của người dùng vào bối cảnh an ninh (Security Context) của Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        // Nếu không có Token hoặc sai định dạng thì kiểm tra query param (dùng cho SSE)
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            // Fallback: lấy token từ query param '?token=...' (EventSource không gửi được header)
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.isBlank()) {
                jwt = tokenParam;
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 2. Giải mã chuỗi JWT ra thành Claims (ném exception nếu token không hợp lệ)
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

                    // Gắn thêm thông tin request (IP, session ID) vào authentication token
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Nạp Authentication đó vào ContextHolder.
                    // Bất kỳ đoạn logic nào phía sau cũng sẽ biết Request này đại diện cho "username" này.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            logger.debug("JWT token expired: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token đã hết hạn");
            return;
        } catch (SignatureException e) {
            logger.debug("JWT signature validation failed: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token signature không hợp lệ");
            return;
        } catch (MalformedJwtException e) {
            logger.debug("JWT token malformed: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token format không hợp lệ");
            return;
        } catch (Exception e) {
            logger.debug("JWT validation error: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token không hợp lệ");
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Ghi thẳng JSON error response vào HttpServletResponse với status 401.
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
