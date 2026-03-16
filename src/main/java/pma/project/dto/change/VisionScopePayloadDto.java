package pma.project.dto.change;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisionScopePayloadDto {
    private Integer visionScopeId;
    private String content;
}
