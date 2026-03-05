package pma.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.user.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

}
