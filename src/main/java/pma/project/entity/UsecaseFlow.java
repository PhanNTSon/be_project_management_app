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
@Table(name = "UsecaseFlow")
@Getter
@NoArgsConstructor
public class UsecaseFlow {
    @Id
    @Column(name = "FlowId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flowId;

    @ManyToOne
    @JoinColumn(name = "UsecaseId", nullable = false)
    private Usecase usecase;

    @Column(name = "FlowType", nullable = false, length = 20)
    private String flowType;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "IsAlternative", nullable = false)
    private boolean isAlternative = false;

    public void setUsecase(Usecase usecase) {
        if (usecase == null) {
            throw new IllegalArgumentException("Usecase cannot be null");
        }
        this.usecase = usecase;
    }

    public void setFlowType(String flowType) {
        if (flowType == null || flowType.trim().isEmpty()) {
            throw new IllegalArgumentException("FlowType cannot be null or empty");
        }
        if (!flowType.equals("NORMAL") && !flowType.equals("ALTERNATIVE")) {
            throw new IllegalArgumentException("FlowType must be NORMAL or ALTERNATIVE");
        }
        this.flowType = flowType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAlternative(boolean alternative) {
        isAlternative = alternative;
    }
}
