package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.UsecaseFlow;

@Repository
public interface UsecaseFlowRepository extends JpaRepository<UsecaseFlow, Integer> {
}
