package pma.project.entity.core;

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
@Table(name = "NonFunctionalRequirement")
@Getter
@NoArgsConstructor
public class NonFunctionalRequirement {
    @Id
    @Column(name = "RequirementId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requirementId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "Category", nullable = false, length = 50)
    private String category;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        if (!category.equals("USABILITY") && !category.equals("PERFORMANCE") && 
            !category.equals("SECURITY") && !category.equals("SCALABILITY")) {
            throw new IllegalArgumentException("Category must be USABILITY, PERFORMANCE, SECURITY, or SCALABILITY");
        }
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
