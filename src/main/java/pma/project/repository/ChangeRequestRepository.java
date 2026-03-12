package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.change.ChangeRequest;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Integer> {
}
