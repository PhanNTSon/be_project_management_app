package pma.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConstraintDto {
    private Integer constraintId;
    private String type;
    private String description;
}
