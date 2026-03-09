package pma.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pma.user.entity.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    private User user;
    private String accessToken;
    private String refreshToken;
}
