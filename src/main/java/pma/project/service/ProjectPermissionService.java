package pma.project.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import pma.project.repository.ProjectMemberRepository;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectMemberId;
import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.exception.CustomException.ForbiddenAccessException;

@Service
@RequiredArgsConstructor
public class ProjectPermissionService {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     * Kiểm tra xem User trong Project có chứa mã Quyền (Permission Code) được yêu cầu hay không.
     * @param userId ID của User
     * @param projectId ID của Project
     * @param requiredPermissionCode Mã quyền cần kiểm tra, ví dụ: "EDIT_PROJECT_INFO"
     * @throws ProjectNotFoundException nếu user không nằm trong project
     * @throws ForbiddenAccessException nếu user nằm trong project nhưng ROLE không chứa PermissionCode
     */
    public void validatePermission(Long userId, Integer projectId, String requiredPermissionCode) {
        ProjectMember member = projectMemberRepository.findById(new ProjectMemberId(projectId, userId))
                .orElseThrow(() -> new ProjectNotFoundException("User is not a member of this project"));

        boolean hasPermission = member.getProjectRole().getPermissions().stream()
                .anyMatch(p -> p.getCode().equals(requiredPermissionCode));

        if (!hasPermission) {
            throw new ForbiddenAccessException("You do not have the required permission: " + requiredPermissionCode);
        }
    }

    /**
     * Kiểm tra nhanh xem User có Role cụ thể nào đó (như MAINTAINER) không.
     */
    public boolean hasRole(Long userId, Integer projectId, String roleName) {
        return projectMemberRepository.findById(new ProjectMemberId(projectId, userId))
                .map(m -> m.getProjectRole().getName().equalsIgnoreCase(roleName))
                .orElse(false); // Nếu không thuộc project, trả về false luôn.
    }
}
