package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.UsecaseFlow;

import java.util.List;

@Repository
public interface UsecaseFlowRepository extends JpaRepository<UsecaseFlow, Integer> {
    List<UsecaseFlow> findByUsecase_UsecaseId(Integer usecaseId);
    void deleteByUsecase_UsecaseId(Integer usecaseId);
}
