package pma.project.entity.usecase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import pma.project.entity.core.Project;

@Entity
@Table(name = "BusinessRule")
@Getter
@NoArgsConstructor
public class BusinessRule {
    @Id
    @Column(name = "RuleId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ruleId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "RuleDescription", columnDefinition = "NVARCHAR(MAX)")
    private String ruleDescription;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }
}
