package pma.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import pma.common.exception.CustomException.EmailAlreadyExistException;
import pma.common.security.JwtService;
import pma.user.dto.request.RequestLoginDto;
import pma.user.dto.request.RequestRegisterDto;
import pma.user.dto.response.ResponseLoginDto;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(RequestRegisterDto registerDto) {

        if (userRepo.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException();
        }

        User newUser = new User(registerDto.getEmail(),
                registerDto.getUsername(),
                passwordEncoder.encode(registerDto.getPassword()));

        userRepo.save(newUser);
    }

    public ResponseLoginDto loginUser(RequestLoginDto loginDto) throws Exception {
        User user = userRepo.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new Exception());

        if (!user.getPassword().equals(loginDto.getPassword())) {
            throw new Exception();
        }

        String accessToken = jwtService.generateToken(user.getUsername());

        return new ResponseLoginDto(user.getUsername(), user.getFullName(), user.getEmail(), accessToken, "Bearer");
    }

}
