package pma.project.entity.core;

import java.time.LocalDateTime;
import pma.project.entity.member.ProjectMember;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import pma.user.entity.User;

@Entity
@Table(name = "Project")
@Getter
@NoArgsConstructor
public class Project {
    @Id
    @Column(name = "ProjectId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Column(name = "ProjectName", nullable = false, length = 255)
    private String projectName;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "ContextDiagramUrl", columnDefinition = "TEXT")
    private String contextDiagramUrl;

    @ManyToOne
    @JoinColumn(name = "OwnerId", nullable = false)
    private User owner;

    @Column(name = "TemplateId")
    private Integer templateId;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    public Project(String projectName, User owner) {
        setProjectName(projectName);
        setOwner(owner);
        this.createdAt = LocalDateTime.now();
        this.members = new HashSet<>();
    }

    public void setProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("ProjectName cannot be null or empty");
        }
        if (projectName.length() > 255) {
            throw new IllegalArgumentException("ProjectName cannot exceed 255 characters");
        }
        this.projectName = projectName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContextDiagramUrl(String contextDiagramUrl) {
        this.contextDiagramUrl = contextDiagramUrl;
    }

    public void setOwner(User owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setMembers(Set<ProjectMember> members) {
        this.members = members;
    }
}
