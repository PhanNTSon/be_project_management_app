package pma.project.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UsecaseBusinessRule")
@Getter
@NoArgsConstructor
public class UsecaseBusinessRule {
    @EmbeddedId
    private UsecaseBusinessRuleId id;

    @ManyToOne
    @MapsId("usecaseId")
    @JoinColumn(name = "UsecaseId", nullable = false)
    private Usecase usecase;

    @ManyToOne
    @MapsId("ruleId")
    @JoinColumn(name = "RuleId", nullable = false)
    private BusinessRule businessRule;

    public UsecaseBusinessRule(Usecase usecase, BusinessRule rule) {
        setUsecase(usecase);
        setBusinessRule(rule);
        this.id = new UsecaseBusinessRuleId(usecase.getUsecaseId(), rule.getRuleId());
    }

    public void setId(UsecaseBusinessRuleId id) {
        this.id = id;
    }

    public void setUsecase(Usecase usecase) {
        if (usecase == null) {
            throw new IllegalArgumentException("Usecase cannot be null");
        }
        this.usecase = usecase;
    }

    public void setBusinessRule(BusinessRule businessRule) {
        if (businessRule == null) {
            throw new IllegalArgumentException("BusinessRule cannot be null");
        }
        this.businessRule = businessRule;
    }
}
