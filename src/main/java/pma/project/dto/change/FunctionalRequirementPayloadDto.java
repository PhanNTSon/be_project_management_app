package pma.project.dto.change;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalRequirementPayloadDto {
    private Integer requirementId;
    private String title;
    private String description;
}
