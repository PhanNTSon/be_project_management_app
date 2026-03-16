package pma.user.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;

    public UserRoleId() {
    }

    public UserRoleId(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserRoleId that = (UserRoleId) o;

        if (!userId.equals(that.userId))
            return false;
        return roleId.equals(that.roleId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + roleId.hashCode();
        return result;
    }
}
