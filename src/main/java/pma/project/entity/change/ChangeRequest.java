package pma.project.entity.change;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

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

import pma.project.entity.core.Project;
import pma.user.entity.User;

@Entity
@Table(name = "ChangeRequest")
@Getter
@NoArgsConstructor
public class ChangeRequest {
    @Id
    @Column(name = "ChangeRequestId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer changeRequestId;

    @ManyToOne
    @JoinColumn(name = "ProjectId", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "RequesterId", nullable = false)
    private User requester;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "[Description]", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "[Status]", nullable = false, length = 20)
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "ReviewedBy")
    private User reviewedBy;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeItem> changeItems = new ArrayList<>();

    public ChangeRequest(Project project, User requester, String title, String description) {
        setProject(project);
        setRequester(requester);
        setTitle(title);
        setDescription(description);
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public void setProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        this.project = project;
    }

    public void setRequester(User requester) {
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }
        this.requester = requester;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Title cannot exceed 255 characters");
        }
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        String upperStatus = status.toUpperCase();
        if (!upperStatus.equals("PENDING") && !upperStatus.equals("APPROVED") && !upperStatus.equals("REJECTED")) {
            throw new IllegalArgumentException("Status must be PENDING, APPROVED, or REJECTED");
        }
        this.status = upperStatus;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
