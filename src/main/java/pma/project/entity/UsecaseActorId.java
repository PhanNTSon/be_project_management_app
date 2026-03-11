package pma.project.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsecaseActorId implements Serializable {
    private Integer usecaseId;
    private Integer actorId;

    public void setUsecaseId(Integer usecaseId) {
        this.usecaseId = usecaseId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }
}
