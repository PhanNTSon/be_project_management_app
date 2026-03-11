package pma.project.entity;

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

import pma.user.entity.User;

@Entity
@Table(name = "Usecase")
@Getter
@NoArgsConstructor
public class Usecase {
    @Id
    @Column(name = "UsecaseId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usecaseId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "CreatedBy", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "FunctionalRequirementId", nullable = false)
    private FunctionalRequirement functionalRequirement;

    @Column(name = "UsecaseName", nullable = false, length = 255)
    private String usecaseName;

    @Column(name = "Precondition", columnDefinition = "NVARCHAR(MAX)")
    private String precondition;

    @Column(name = "Postcondition", columnDefinition = "NVARCHAR(MAX)")
    private String postcondition;

    @Column(name = "Exceptions", columnDefinition = "NVARCHAR(MAX)")
    private String exceptions;

    @Column(name = "[Priority]", length = 50)
    private String priority;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setCreatedBy(User createdBy) {
        if (createdBy == null) {
            throw new IllegalArgumentException("CreatedBy user cannot be null");
        }
        this.createdBy = createdBy;
    }

    public void setFunctionalRequirement(FunctionalRequirement functionalRequirement) {
        if (functionalRequirement == null) {
            throw new IllegalArgumentException("FunctionalRequirement cannot be null");
        }
        this.functionalRequirement = functionalRequirement;
    }

    public void setUsecaseName(String usecaseName) {
        if (usecaseName == null || usecaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("UsecaseName cannot be null or empty");
        }
        if (usecaseName.length() > 255) {
            throw new IllegalArgumentException("UsecaseName cannot exceed 255 characters");
        }
        this.usecaseName = usecaseName;
    }

    public void setPrecondition(String precondition) {
        this.precondition = precondition;
    }

    public void setPostcondition(String postcondition) {
        this.postcondition = postcondition;
    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public void setPriority(String priority) {
        if (priority != null && !priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW")) {
            throw new IllegalArgumentException("Priority must be HIGH, MEDIUM, LOW, or null");
        }
        this.priority = priority;
    }
}
