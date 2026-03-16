package pma.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserInvitationDto {
    private Integer invitationId;
    private Integer projectId;
    private String projectName;
    private String status;
    private LocalDateTime sentAt;
}
