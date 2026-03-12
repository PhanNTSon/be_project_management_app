package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.BusinessRule;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Integer> {
    List<BusinessRule> findByProject_ProjectId(Integer projectId);
}
