package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.NonFunctionalRequirement;

@Repository
public interface NonFunctionalRequirementRepository extends JpaRepository<NonFunctionalRequirement, Integer> {
}
