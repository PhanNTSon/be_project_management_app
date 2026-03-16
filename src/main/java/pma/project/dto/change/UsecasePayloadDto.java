package pma.project.dto.change;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsecasePayloadDto {
    private Integer usecaseId;
    private String usecaseName;
    private String precondition;
    private String postcondition;
    private String exceptions;
    private String priority;
    private String actor;
    private String functionRelId;
    private List<String> normalFlows;
    private List<String> alterFlows;
    private List<String> linkedBusinessRuleIds;
    private String diagramUrl;
}
