package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.UsecaseBusinessRule;
import pma.project.entity.usecase.UsecaseBusinessRuleId;

import java.util.List;

@Repository
public interface UsecaseBusinessRuleRepository extends JpaRepository<UsecaseBusinessRule, UsecaseBusinessRuleId> {
    List<UsecaseBusinessRule> findByUsecase_UsecaseId(Integer usecaseId);
    void deleteByUsecase_UsecaseId(Integer usecaseId);
}
