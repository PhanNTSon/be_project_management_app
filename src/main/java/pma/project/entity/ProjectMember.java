package pma.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import pma.user.entity.User;

@Entity
@Table(name = "ProjectMember")
@Getter
@NoArgsConstructor
public class ProjectMember {
    @EmbeddedId
    private ProjectMemberId id;

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Role", nullable = false, length = 100)
    private String role;

    public ProjectMember(Project project, User user, String role) {
        setProject(project);
        setUser(user);
        setRole(role);
        this.id = new ProjectMemberId(project.getProjectId(), user.getUserId());
    }

    public void setId(ProjectMemberId id) {
        this.id = id;
    }

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
    }

    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        if (role.length() > 100) {
            throw new IllegalArgumentException("Role cannot exceed 100 characters");
        }
        this.role = role;
    }
}
