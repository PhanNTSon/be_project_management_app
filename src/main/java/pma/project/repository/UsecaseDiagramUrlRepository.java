package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.UsecaseDiagramUrl;

@Repository
public interface UsecaseDiagramUrlRepository extends JpaRepository<UsecaseDiagramUrl, Integer> {
}
