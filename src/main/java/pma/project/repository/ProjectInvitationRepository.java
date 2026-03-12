package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.ProjectInvitation;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
}
