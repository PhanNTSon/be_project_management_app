package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectMemberId;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    Optional<ProjectMember> findByProject_ProjectIdAndUser_UserId(Integer projectId, Long userId);

    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user JOIN FETCH pm.projectRole WHERE pm.id.projectId = :projectId")
    List<ProjectMember> findAllByProjectIdWithUserAndRole(@Param("projectId") Integer projectId);

    Integer countByIdProjectId(Integer projectId);
}
