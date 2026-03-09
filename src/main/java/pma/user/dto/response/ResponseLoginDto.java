package pma.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResponseLoginDto {
    
    private String username;
    private String fullName;
    private String email;
    private String accessToken;
    private String tokenType = "Bearer";
}
