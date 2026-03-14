package pma.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserDto {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private boolean isActive;
}
