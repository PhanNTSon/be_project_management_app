package pma.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pma.common.exception.ApiException;
import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.mapper.*;
import pma.project.dto.request.RequestCreateProjectDto;
import pma.project.dto.response.*;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.project.entity.core.*;
import pma.project.entity.member.ProjectMember;
import pma.project.entity.usecase.Usecase;
import pma.project.entity.usecase.UsecaseActor;
import pma.project.entity.usecase.UsecaseBusinessRule;
import pma.project.entity.usecase.UsecaseFlow;
import pma.project.repository.*;
import pma.user.entity.User;
import pma.user.repository.UserRepo;


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

    // Child-table repos for usecase enrichment
    private final UsecaseFlowRepository usecaseFlowRepository;
    private final UsecaseBusinessRuleRepository usecaseBusinessRuleRepository;
    private final UsecaseActorRepository usecaseActorRepository;
    private final UsecaseDiagramUrlRepository usecaseDiagramUrlRepository;

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
        return usecaseRepository.findByProject_ProjectId(projectId).stream()
                .map(this::toEnrichedUsecaseDto)
                .collect(Collectors.toList());
    }

    public List<ResponseFunctionalReqDto> getFunctionalRequirements(Integer projectId) {
        return functionalReqMapper.toDtoList(functionalRequirementRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponseNonFunctionalReqDto> getNonFunctionalRequirements(Integer projectId) {
        return nonFunctionalReqMapper.toDtoList(nonFunctionalRequirementRepository.findByProject_ProjectId(projectId));
    }

    public List<ResponsePermissionDto> getPermissions(Integer projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, userId)
                .orElseThrow(ProjectNotFoundException::new);
        return member.getProjectRole().getPermissions().stream()
                .map(p -> new ResponsePermissionDto(p.getCode(), p.getDescription()))
                .toList();
    }

    public ResponseRoleDto getUserRole(Integer projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, userId)
                .orElseThrow(ProjectNotFoundException::new);
        return new ResponseRoleDto(member.getProjectRole().getName());
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @Transactional
    public void updateContextDiagramUrl(Long userId, Integer projectId, pma.project.dto.request.RequestUpdateContextDiagramDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
        
        // Ensure user is part of the project before allowing update (or rely on @PreAuthorize/Permissions elsewhere)
        projectMemberRepository.findByProject_ProjectIdAndUser_UserId(projectId, userId)
                .orElseThrow(ProjectNotFoundException::new);

        project.setContextDiagramUrl(dto.getUrl());
        projectRepository.save(project);
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

    // =====================================================================
    // PRIVATE: USECASE ENRICHMENT
    // =====================================================================

    /**
     * Enriches a Usecase entity into a full ResponseUsecaseDto by joining
     * UsecaseFlow, UsecaseBusinessRule, and UsecaseActor child tables.
     */
    private ResponseUsecaseDto toEnrichedUsecaseDto(Usecase uc) {
        ResponseUsecaseDto dto = usecaseMapper.toDto(uc);

        // functionRelId — from @ManyToOne navigation (never null per entity constraint)
        if (uc.getFunctionalRequirement() != null) {
            dto.setFunctionRelId(uc.getFunctionalRequirement().getRequirementId());
        }

        Integer id = uc.getUsecaseId();

        // normalFlows and alterFlows — from UsecaseFlow child table
        List<UsecaseFlow> flows = usecaseFlowRepository.findByUsecase_UsecaseId(id);
        dto.setNormalFlows(flows.stream()
                .filter(f -> "NORMAL".equals(f.getFlowType()))
                .map(UsecaseFlow::getDescription)
                .collect(Collectors.toList()));
        dto.setAlterFlows(flows.stream()
                .filter(f -> "ALTERNATIVE".equals(f.getFlowType()))
                .map(UsecaseFlow::getDescription)
                .collect(Collectors.toList()));

        // linkedBusinessRuleIds — from UsecaseBusinessRule join table
        List<UsecaseBusinessRule> brLinks = usecaseBusinessRuleRepository.findByUsecase_UsecaseId(id);
        dto.setLinkedBusinessRuleIds(brLinks.stream()
                .map(link -> link.getBusinessRule().getRuleId())
                .collect(Collectors.toList()));

        // actor — first linked Actor's name (frontend treats actor as a single string)
        List<UsecaseActor> actorLinks = usecaseActorRepository.findByUsecase_UsecaseId(id);
        dto.setActor(actorLinks.isEmpty() ? "" : actorLinks.get(0).getActor().getActorName());

        // diagramUrl — from UsecaseDiagramUrl mapped entity
        usecaseDiagramUrlRepository.findById(id).ifPresent(diagram -> dto.setDiagramUrl(diagram.getDiagramUrl()));

        return dto;
    }
}
