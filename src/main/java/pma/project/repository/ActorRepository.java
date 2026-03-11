package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.Actor;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer> {
}
