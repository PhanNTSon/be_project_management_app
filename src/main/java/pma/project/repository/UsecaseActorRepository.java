package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.UsecaseActor;
import pma.project.entity.UsecaseActorId;

@Repository
public interface UsecaseActorRepository extends JpaRepository<UsecaseActor, UsecaseActorId> {
}
