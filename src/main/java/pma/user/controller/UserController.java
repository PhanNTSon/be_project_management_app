package pma.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pma.user.dto.response.ResponseUserDto;
import pma.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ResponseUserDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        ResponseUserDto userDto = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userDto);
    }
}

