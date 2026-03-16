package pma.project.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Permission")
@Getter
@NoArgsConstructor
public class Permission {
    @Id
    @Column(name = "PermissionId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permissionId;

    @Column(name = "Code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "[Description]", length = 255)
    private String description;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Permission(String code, String description) {
        setCode(code);
        setDescription(description);
        this.createdAt = LocalDateTime.now();
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
        if (code.length() > 100) {
            throw new IllegalArgumentException("Code cannot exceed 100 characters");
        }
        this.code = code;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Description cannot exceed 255 characters");
        }
        this.description = description;
    }
}
