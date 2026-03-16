package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.Constraint;

@Repository
public interface ConstraintRepository extends JpaRepository<Constraint, Integer> {
    List<Constraint> findByProject_ProjectId(Integer projectId);
}
