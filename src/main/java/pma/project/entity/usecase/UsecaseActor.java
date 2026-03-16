package pma.project.entity.usecase;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "UsecaseActor")
@Getter
@NoArgsConstructor
public class UsecaseActor {
    @EmbeddedId
    private UsecaseActorId id;

    @ManyToOne
    @MapsId("usecaseId")
    @JoinColumn(name = "UsecaseId", nullable = false)
    private Usecase usecase;

    @ManyToOne
    @MapsId("actorId")
    @JoinColumn(name = "ActorId", nullable = false)
    private Actor actor;

    public UsecaseActor(Usecase usecase, Actor actor) {
        setUsecase(usecase);
        setActor(actor);
        this.id = new UsecaseActorId(usecase.getUsecaseId(), actor.getActorId());
    }

    public void setId(UsecaseActorId id) {
        this.id = id;
    }

    public void setUsecase(Usecase usecase) {
        if (usecase == null) {
            throw new IllegalArgumentException("Usecase cannot be null");
        }
        this.usecase = usecase;
    }

    public void setActor(Actor actor) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor cannot be null");
        }
        this.actor = actor;
    }
}
