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

@Entity
@Table(name = "[Constraint]")
@Getter
@NoArgsConstructor
public class Constraint {
    @Id
    @Column(name = "ConstraintId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer constraintId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "[Type]", nullable = false, length = 100)
    private String type;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (type.length() > 100) {
            throw new IllegalArgumentException("Type cannot exceed 100 characters");
        }
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
