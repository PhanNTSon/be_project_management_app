package pma.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pma.project.entity.change.ChangeItem;
import java.util.List;

@Repository
public interface ChangeItemRepository extends JpaRepository<ChangeItem, Integer> {
    List<ChangeItem> findByChangeRequest_ChangeRequestId(Integer changeRequestId);
}
