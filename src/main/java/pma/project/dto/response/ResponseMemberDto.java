package pma.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMemberDto {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String roleName;
    private LocalDateTime joinedAt;
}
