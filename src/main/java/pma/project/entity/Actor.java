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
@Table(name = "Actor")
@Getter
@NoArgsConstructor
public class Actor {
    @Id
    @Column(name = "ActorId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actorId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "ActorName", nullable = false, length = 255)
    private String actorName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setActorName(String actorName) {
        if (actorName == null || actorName.trim().isEmpty()) {
            throw new IllegalArgumentException("ActorName cannot be null or empty");
        }
        if (actorName.length() > 255) {
            throw new IllegalArgumentException("ActorName cannot exceed 255 characters");
        }
        this.actorName = actorName;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
