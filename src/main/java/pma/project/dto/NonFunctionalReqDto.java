package pma.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NonFunctionalReqDto {
    private Integer requirementId;
    private String category;
    private String description;
}
