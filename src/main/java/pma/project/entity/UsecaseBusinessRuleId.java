package pma.project.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsecaseBusinessRuleId implements Serializable {
    private Integer usecaseId;
    private Integer ruleId;

    public void setUsecaseId(Integer usecaseId) {
        this.usecaseId = usecaseId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }
}
