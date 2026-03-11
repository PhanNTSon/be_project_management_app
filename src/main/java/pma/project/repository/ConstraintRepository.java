package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.Constraint;

@Repository
public interface ConstraintRepository extends JpaRepository<Constraint, Integer> {
}
