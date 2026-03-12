package pma.project.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import pma.project.entity.core.Project;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ProjectInvitation")
@Getter
@NoArgsConstructor
public class ProjectInvitation {
    @Id
    @Column(name = "InvitationId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer invitationId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "Status", nullable = false, length = 50)
    private String status = "Pending";

    @Column(name = "SentAt", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
        this.email = email;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (status.length() > 50) {
            throw new IllegalArgumentException("Status cannot exceed 50 characters");
        }
        this.status = status;
    }

    public void setSentAt(LocalDateTime sentAt) {
        if (sentAt == null) {
            throw new IllegalArgumentException("SentAt cannot be null");
        }
        this.sentAt = sentAt;
    }
}
