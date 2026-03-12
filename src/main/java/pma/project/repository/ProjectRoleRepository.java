package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.ProjectRole;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Integer> {
}
