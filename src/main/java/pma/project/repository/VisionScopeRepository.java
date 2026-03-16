package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.VisionScope;

@Repository
public interface VisionScopeRepository extends JpaRepository<VisionScope, Integer> {
    List<VisionScope> findByProject_ProjectId(Integer projectId);
}
