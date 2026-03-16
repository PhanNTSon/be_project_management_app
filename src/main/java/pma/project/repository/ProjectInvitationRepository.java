package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.ProjectInvitation;

import java.util.List;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
    
    List<ProjectInvitation> findByProject_ProjectId(Integer projectId);
    
    List<ProjectInvitation> findByEmail(String email);
    
    Integer countByProject_ProjectIdAndStatus(Integer projectId, String status);
    
    boolean existsByProject_ProjectIdAndEmailAndStatus(Integer projectId, String email, String status);
}
