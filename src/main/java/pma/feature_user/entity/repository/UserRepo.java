package pma.feature_user.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.feature_user.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

}
