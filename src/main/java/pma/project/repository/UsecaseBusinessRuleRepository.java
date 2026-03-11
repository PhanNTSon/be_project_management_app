package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.UsecaseBusinessRule;
import pma.project.entity.UsecaseBusinessRuleId;

@Repository
public interface UsecaseBusinessRuleRepository extends JpaRepository<UsecaseBusinessRule, UsecaseBusinessRuleId> {
}
