package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.FunctionalRequirement;

@Repository
public interface FunctionalRequirementRepository extends JpaRepository<FunctionalRequirement, Integer> {
}
