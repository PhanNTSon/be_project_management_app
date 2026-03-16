package pma.project.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeItemSubmitDto {
    private String entityType; // e.g. "USECASE", "VISION_SCOPE"
    private Integer entityId; // Id of the entity being changed
    private String operation; // "CREATE", "UPDATE", "DELETE"
    private String fieldName; // e.g., "title", "description"
    private String oldValue; // JSON string of old value
    private String newValue; // JSON string of proposed new value
}
