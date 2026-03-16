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
public class ResponseInvitationDto {
    private Integer invitationId;
    private String email;
    private String status;
    private LocalDateTime sentAt;
}
