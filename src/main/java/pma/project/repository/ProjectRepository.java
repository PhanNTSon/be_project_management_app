package pma.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pma.project.entity.core.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("SELECT p FROM Project p LEFT JOIN p.members m WHERE p.owner.userId = :userId OR m.user.userId = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);
}
