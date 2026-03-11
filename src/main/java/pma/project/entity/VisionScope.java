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
@Table(name = "VisionScope")
@Getter
@NoArgsConstructor
public class VisionScope {
    @Id
    @Column(name = "VisionScopeId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer visionScopeId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
