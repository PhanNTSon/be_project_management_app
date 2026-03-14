package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectMemberId;

import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    Optional<ProjectMember> findByProject_ProjectIdAndUser_UserId(Integer projectId, Long userId);
}
