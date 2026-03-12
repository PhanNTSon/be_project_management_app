package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.Usecase;

@Repository
public interface UsecaseRepository extends JpaRepository<Usecase, Integer> {
    List<Usecase> findByProject_ProjectId(Integer projectId);
}
