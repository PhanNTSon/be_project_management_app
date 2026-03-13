package pma.project.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseChangeRequestDto {
    private Integer changeRequestId;
    private String title;
    private String description;
    private String status;
    private String requesterName;
    private LocalDateTime createdAt;
    private List<ResponseChangeItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseChangeItemDto {
        private Integer changeItemId;
        private String entityType;
        private Integer entityId;
        private String operation;
        private String fieldName;
        private String oldValue;
        private String newValue;
    }
}
