package pma.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContextPromptDto {
    private String projectName;
    private String description;
    private List<String> visionScopes;
    private List<String> actorList;
}
