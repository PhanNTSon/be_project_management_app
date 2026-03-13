package pma.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFunctionalReqDto {
    private Integer requirementId;
    private String title;
    private String description;
}
