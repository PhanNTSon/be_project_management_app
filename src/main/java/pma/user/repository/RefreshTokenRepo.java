package pma.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.user.entity.RefreshToken;
import pma.user.entity.User;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    void deleteByUser(User user);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);
}
