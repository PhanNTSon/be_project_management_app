package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.UsecaseActor;
import pma.project.entity.usecase.UsecaseActorId;

import java.util.List;

@Repository
public interface UsecaseActorRepository extends JpaRepository<UsecaseActor, UsecaseActorId> {
    List<UsecaseActor> findByUsecase_UsecaseId(Integer usecaseId);
}
