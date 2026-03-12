package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.NonFunctionalRequirement;

@Repository
public interface NonFunctionalRequirementRepository extends JpaRepository<NonFunctionalRequirement, Integer> {
    List<NonFunctionalRequirement> findByProject_ProjectId(Integer projectId);
}
