package pma.project.entity.change;

import java.time.LocalDateTime;

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
@Table(name = "ChangeItem")
@Getter
@NoArgsConstructor
public class ChangeItem {
    @Id
    @Column(name = "ChangeItemId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer changeItemId;

    @ManyToOne
    @JoinColumn(name = "ChangeRequestId", nullable = false)
    private ChangeRequest changeRequest;

    @Column(name = "EntityType", nullable = false, length = 50)
    private String entityType;

    @Column(name = "EntityId")
    private Integer entityId;

    @Column(name = "Operation", nullable = false, length = 10)
    private String operation;

    @Column(name = "FieldName", length = 100)
    private String fieldName;

    @Column(name = "OldValue", columnDefinition = "NVARCHAR(MAX)")
    private String oldValue;

    @Column(name = "NewValue", columnDefinition = "NVARCHAR(MAX)")
    private String newValue;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChangeItem(ChangeRequest changeRequest, String entityType, String operation) {
        setChangeRequest(changeRequest);
        setEntityType(entityType);
        setOperation(operation);
        this.createdAt = LocalDateTime.now();
    }

    public void setChangeRequest(ChangeRequest changeRequest) {
        if (changeRequest == null) {
            throw new IllegalArgumentException("ChangeRequest cannot be null");
        }
        this.changeRequest = changeRequest;
    }

    public void setEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            throw new IllegalArgumentException("EntityType cannot be null or empty");
        }
        if (entityType.length() > 50) {
            throw new IllegalArgumentException("EntityType cannot exceed 50 characters");
        }
        this.entityType = entityType;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public void setOperation(String operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
        String upperOp = operation.toUpperCase();
        if (!upperOp.equals("CREATE") && !upperOp.equals("UPDATE") && !upperOp.equals("DELETE")) {
            throw new IllegalArgumentException("Operation must be CREATE, UPDATE, or DELETE");
        }
        this.operation = upperOp;
    }

    public void setFieldName(String fieldName) {
        if (fieldName != null && fieldName.length() > 100) {
            throw new IllegalArgumentException("FieldName cannot exceed 100 characters");
        }
        this.fieldName = fieldName;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
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
}
