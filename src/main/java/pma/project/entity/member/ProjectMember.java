package pma.project.entity.member;

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
import pma.project.entity.core.Project;

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

    @ManyToOne
    @JoinColumn(name = "ProjectRoleId", nullable = false)
    private ProjectRole projectRole;

    public ProjectMember(Project project, User user, ProjectRole projectRole) {
        setProject(project);
        setUser(user);
        setProjectRole(projectRole);
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

    public void setProjectRole(ProjectRole projectRole) {
        if (projectRole == null) {
            throw new IllegalArgumentException("ProjectRole cannot be null");
        }
        this.projectRole = projectRole;
    }
}
