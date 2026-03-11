package pma.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UsecaseDiagramUrl")
@Getter
@NoArgsConstructor
public class UsecaseDiagramUrl {
    @Id
    private Integer usecaseId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "UsecaseId")
    private Usecase usecase;

    @Column(name = "DiagramUrl", columnDefinition = "TEXT")
    private String diagramUrl;

    public void setUsecaseId(Integer usecaseId) {
        this.usecaseId = usecaseId;
    }

    public void setUsecase(Usecase usecase) {
        if (usecase == null) {
            throw new IllegalArgumentException("Usecase cannot be null");
        }
        this.usecase = usecase;
    }

    public void setDiagramUrl(String diagramUrl) {
        this.diagramUrl = diagramUrl;
    }
}
