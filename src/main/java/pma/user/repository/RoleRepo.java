package pma.user.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.user.entity.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

}
