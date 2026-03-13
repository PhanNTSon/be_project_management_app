package pma.project.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pma.common.exception.ApiException;
import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.mapper.*;
import pma.project.dto.request.RequestCreateProjectDto;
import pma.project.dto.response.*;
import pma.project.entity.core.*;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.member.ProjectMemberId;
import pma.project.repository.*;
import pma.user.entity.User;
import pma.user.repository.UserRepo;
import pma.common.exception.CustomException.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final VisionScopeRepository visionScopeRepository;
    private final ConstraintRepository constraintRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final UsecaseRepository usecaseRepository;
    private final FunctionalRequirementRepository functionalRequirementRepository;
    private final NonFunctionalRequirementRepository nonFunctionalRequirementRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepo userRepository;
    private final ProjectRoleRepository projectRoleRepository;

    // --- Mappers ---
    private final ProjectMapper projectMapper;
    private final VisionScopeMapper visionScopeMapper;
    private final ConstraintMapper constraintMapper;
    private final UsecaseMapper usecaseMapper;
    private final FunctionalReqMapper functionalReqMapper;
    private final NonFunctionalReqMapper nonFunctionalReqMapper;
    private final BusinessRuleMapper businessRuleMapper;

    // =====================================================================
    // QUERY METHODS
    // =====================================================================

    public List<ResponseProjectListDto> getProjectsByUserId(Long userId) {
        return projectMapper.toDtoList(projectRepository.findProjectsByUserId(userId));
    }

    public String getContextDiagramUrl(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        return project.getContextDiagramUrl();
    }

    public List<ResponseVisionScopeDto> getVisionScopes(Integer projectId) {
        return visionScopeMapper.toDtoList(visionScopeRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseConstraintDto> getConstraints(Integer projectId) {
        return constraintMapper.toDtoList(constraintRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseBusinessRuleDto> getBusinessRules(Integer projectId) {
        return businessRuleMapper.toDtoList(businessRuleRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseUsecaseDto> getUsecases(Integer projectId) {
        return usecaseMapper.toDtoList(usecaseRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseFunctionalReqDto> getFunctionalRequirements(Integer projectId) {
        return functionalReqMapper.toDtoList(functionalRequirementRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseNonFunctionalReqDto> getNonFunctionalRequirements(Integer projectId) {
        return nonFunctionalReqMapper.toDtoList(nonFunctionalRequirementRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponsePermissionDto> getPermissions(Integer projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findById(new ProjectMemberId(projectId, userId))
                .orElseThrow(ProjectNotFoundException::new);
        return member.getProjectRole().getPermissions().stream()
                .map(p -> new ResponsePermissionDto(p.getCode(), p.getDescription()))
                .toList();
    }

    // =====================================================================
    // CREATE / DELETE
    // =====================================================================

    /**
     * Tạo project mới và gán OWNER cho người tạo.
     */
    @Transactional
    public ResponseProjectListDto createProject(Long ownerId, RequestCreateProjectDto dto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Project project = new Project(dto.getProjectName(), owner);
        project.setDescription(dto.getDescription());
        project = projectRepository.save(project);

        // Gán OWNER role cho người tạo qua ProjectMember
        var ownerRole = projectRoleRepository.findByName("OWNER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "System role OWNER not found. Please check seed data."));

        ProjectMember member = new ProjectMember(project, owner, ownerRole);
        projectMemberRepository.save(member);

        return projectMapper.toDto(project);
    }

    /**
     * Xóa project. Chỉ OWNER mới được phép xóa.
     */
    @Transactional
    public void deleteProject(Long requesterId, Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // Chỉ OWNER của project mới được xóa
        if (!project.getOwner().getUserId().equals(requesterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the project owner can delete the project.");
        }

        projectRepository.delete(project);
    }
}
