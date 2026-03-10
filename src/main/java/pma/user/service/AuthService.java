package pma.user.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepo refreshTokenRepo;

    @Transactional
    public void registerUser(RequestRegisterDto registerDto) {

        if (userRepo.findByUsername(registerDto.getUsername()).isPresent()){
            throw new UsernameAlreadyExistException();
        }

        if (userRepo.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException();
        }

        User newUser = new User(registerDto.getEmail(),
                registerDto.getUsername(),
                passwordEncoder.encode(registerDto.getPassword()));

        userRepo.save(newUser);
    }

    @Transactional
    public LoginResult loginUser(RequestLoginDto loginDto) {
        User user = userRepo.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtService.generateToken(user.getUsername());
        String rawToken = refreshTokenService.generateRandomToken();
        String refreshTokenHash = refreshTokenService.hashToken(rawToken);

        RefreshToken rt = new RefreshToken(user, refreshTokenHash, LocalDateTime.now().plusDays(7));
        refreshTokenRepo.save(rt);

        return new LoginResult(user, accessToken, refreshTokenHash);
    }

}
