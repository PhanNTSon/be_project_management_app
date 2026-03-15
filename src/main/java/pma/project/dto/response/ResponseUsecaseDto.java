package pma.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUsecaseDto {
    private Integer usecaseId;
    private String usecaseName;
    private String precondition;
    private String postcondition;
    private String exceptions;
    private String priority;

    // Relationship fields — populated by ProjectService, not the mapper
    private String actor;
    private Integer functionRelId;
    private List<String> normalFlows;
    private List<String> alterFlows;
    private List<Integer> linkedBusinessRuleIds;
    private String diagramUrl;
}
