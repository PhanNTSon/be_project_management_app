package pma.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import pma.user.dto.request.RequestLoginDto;
import pma.user.dto.request.RequestRegisterDto;
import pma.user.service.AuthService;

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

}
