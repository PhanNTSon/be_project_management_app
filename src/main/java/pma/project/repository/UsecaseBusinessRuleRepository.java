package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.UsecaseBusinessRule;
import pma.project.entity.usecase.UsecaseBusinessRuleId;

@Repository
public interface UsecaseBusinessRuleRepository extends JpaRepository<UsecaseBusinessRule, UsecaseBusinessRuleId> {
    void deleteByUsecase_UsecaseId(Integer usecaseId);
}

