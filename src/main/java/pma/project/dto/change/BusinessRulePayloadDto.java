package pma.project.dto.change;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRulePayloadDto {
    private Integer ruleId;
    private String ruleDescription;
}
