package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pma.project.entity.usecase.Usecase;

@Repository
public interface UsecaseRepository extends JpaRepository<Usecase, Integer> {

    /**
     * Tải toàn bộ Usecase kèm theo child-collections (flows, actors, businessRules)
     * bằng một query JOIN FETCH duy nhất để tránh N+1 problem.
     * diagramUrl là @OneToOne nên được xử lý riêng.
     */
    @Query("""
            SELECT DISTINCT uc FROM Usecase uc
            LEFT JOIN FETCH uc.flows
            LEFT JOIN FETCH uc.usecaseActors ua
            LEFT JOIN FETCH ua.actor
            LEFT JOIN FETCH uc.usecaseBusinessRules ubr
            LEFT JOIN FETCH ubr.businessRule
            WHERE uc.project.projectId = :projectId
            """)
    List<Usecase> findAllWithDetailsByProjectId(@Param("projectId") Integer projectId);
}
