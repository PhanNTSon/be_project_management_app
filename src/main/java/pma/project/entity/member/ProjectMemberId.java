package pma.project.entity.member;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectMemberId implements Serializable {
    private Integer projectId;
    private Long userId;

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
