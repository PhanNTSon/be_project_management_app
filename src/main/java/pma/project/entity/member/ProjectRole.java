package pma.project.entity.member;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ProjectRole")
@Getter
@NoArgsConstructor
public class ProjectRole {
    @Id
    @Column(name = "ProjectRoleId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectRoleId;

    @Column(name = "Name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
        name = "ProjectRolePermission",
        joinColumns = @JoinColumn(name = "ProjectRoleId"),
        inverseJoinColumns = @JoinColumn(name = "PermissionId")
    )
    private Set<Permission> permissions = new HashSet<>();

    public ProjectRole(String name, String description) {
        setName(name);
        setDescription(description);
        this.createdAt = LocalDateTime.now();
        this.permissions = new HashSet<>();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name cannot exceed 100 characters");
        }
        this.name = name;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }
        this.description = description;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }
}
