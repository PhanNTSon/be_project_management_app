package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.member.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
