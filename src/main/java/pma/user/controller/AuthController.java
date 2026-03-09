package pma.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import pma.user.dto.LoginResult;
import pma.user.dto.request.RequestLoginDto;
import pma.user.dto.request.RequestRegisterDto;
import pma.user.dto.response.ResponseLoginDto;
import pma.user.service.AuthService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> postRegisterUserEntity(@Valid @RequestBody RequestRegisterDto registerDto) {
        authService.registerUser(registerDto);
        return ResponseEntity.ok("Register successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseLoginDto> postLoginUserEntity(@Valid @RequestBody RequestLoginDto loginDto) {
        LoginResult loginResult = authService.loginUser(loginDto);

        ResponseCookie springCookie = ResponseCookie.from("refresh_token", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(false) // Set thành true nếu chạy HTTPS
                .sameSite("Strict")
                .path("/api/auth/refresh-token")
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .build();

        ResponseLoginDto response = new ResponseLoginDto(
                loginResult.getUser().getUsername(),
                loginResult.getUser().getFullName(),
                loginResult.getUser().getEmail(),
                loginResult.getAccessToken(),
                "Bearer");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(response);
    }

}
