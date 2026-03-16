package pma.user.service;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.mapper.UserMapper;
import pma.user.dto.response.ResponseUserDto;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    /**
     * Lấy thông tin user dựa trên username
     * Được sử dụng khi client gọi endpoint GET /api/users/me
     */
    public ResponseUserDto getUserByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException());
        return userMapper.toResponseUserDto(user);
    }
}
