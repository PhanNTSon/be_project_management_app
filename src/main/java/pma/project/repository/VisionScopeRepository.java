package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.VisionScope;

@Repository
public interface VisionScopeRepository extends JpaRepository<VisionScope, Integer> {
}
