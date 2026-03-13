package pma.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pma.common.exception.CustomException.ProjectNotFoundException;

import pma.project.dto.response.*;
import pma.project.entity.core.*;
import pma.project.repository.*;

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

    public List<ResponseProjectListDto> getProjectsByUserId(Long userId) {
        return projectRepository.findProjectsByUserId(userId).stream()
                .map(p -> new ResponseProjectListDto(p.getProjectId(), p.getProjectName(), p.getDescription()))
                .collect(Collectors.toList());
    }

    public String getContextDiagramUrl(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());
        return project.getContextDiagramUrl();
    }

    public List<ResponseVisionScopeDto> getVisionScopes(Integer projectId) {
        return visionScopeRepository.findByProject_ProjectId(projectId).stream()
                .map(v -> new ResponseVisionScopeDto(v.getVisionScopeId(), v.getContent()))
                .collect(Collectors.toList());
    }

    public List<ResponseConstraintDto> getConstraints(Integer projectId) {
        return constraintRepository.findByProject_ProjectId(projectId).stream()
                .map(c -> new ResponseConstraintDto(c.getConstraintId(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public List<ResponseBusinessRuleDto> getBusinessRules(Integer projectId) {
        return businessRuleRepository.findByProject_ProjectId(projectId).stream()
                .map(b -> new ResponseBusinessRuleDto(b.getRuleId(), b.getRuleDescription()))
                .collect(Collectors.toList());
    }

    public List<ResponseUsecaseDto> getUsecases(Integer projectId) {
        return usecaseRepository.findByProject_ProjectId(projectId).stream()
                .map(u -> new ResponseUsecaseDto(u.getUsecaseId(), u.getUsecaseName(), u.getPrecondition(), u.getPostcondition(), u.getExceptions(), u.getPriority()))
                .collect(Collectors.toList());
    }

    public List<ResponseFunctionalReqDto> getFunctionalRequirements(Integer projectId) {
        return functionalRequirementRepository.findByProject_ProjectId(projectId).stream()
                .map(f -> new ResponseFunctionalReqDto(f.getRequirementId(), f.getTitle(), f.getDescription()))
                .collect(Collectors.toList());
    }

    public List<ResponseNonFunctionalReqDto> getNonFunctionalRequirements(Integer projectId) {
        return nonFunctionalRequirementRepository.findByProject_ProjectId(projectId).stream()
                .map(n -> new ResponseNonFunctionalReqDto(n.getRequirementId(), n.getCategory(), n.getDescription()))
                .collect(Collectors.toList());
    }

    public List<ResponsePermissionDto> getPermissions(Integer projectId, Long userId) {
        pma.project.entity.member.ProjectMember member = projectMemberRepository.findById(new pma.project.entity.member.ProjectMemberId(projectId, userId))
                .orElseThrow(() -> new ProjectNotFoundException());
        return member.getProjectRole().getPermissions().stream()
                .map(p -> new ResponsePermissionDto(p.getCode(), p.getDescription()))
                .collect(Collectors.toList());
    }
}


