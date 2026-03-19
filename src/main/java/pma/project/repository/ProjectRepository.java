package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
            SELECT p FROM Project p
            WHERE p.owner.userId = :userId
            OR p.projectId IN (
                SELECT m.id.projectId FROM ProjectMember m WHERE m.user.userId = :userId
            )
            """)
    List<Project> findProjectsByUserId(@Param("userId") Long userId);
}
