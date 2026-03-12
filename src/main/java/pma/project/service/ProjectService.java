package pma.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pma.common.exception.CustomException.ProjectNotFoundException;

import pma.project.dto.*;
import pma.project.entity.core.*;
import pma.project.entity.usecase.*;
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

    public List<ProjectListDto> getProjectsByUserId(Long userId) {
        return projectRepository.findProjectsByUserId(userId).stream()
                .map(p -> new ProjectListDto(p.getProjectId(), p.getProjectName(), p.getDescription()))
                .collect(Collectors.toList());
    }

    public String getContextDiagramUrl(Integer projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException());
        return project.getContextDiagramUrl();
    }

    public List<VisionScopeDto> getVisionScopes(Integer projectId) {
        return visionScopeRepository.findByProject_ProjectId(projectId).stream()
                .map(v -> new VisionScopeDto(v.getVisionScopeId(), v.getContent()))
                .collect(Collectors.toList());
    }

    public List<ConstraintDto> getConstraints(Integer projectId) {
        return constraintRepository.findByProject_ProjectId(projectId).stream()
                .map(c -> new ConstraintDto(c.getConstraintId(), c.getDescription()))
                .collect(Collectors.toList());
    }

    public List<BusinessRuleDto> getBusinessRules(Integer projectId) {
        return businessRuleRepository.findByProject_ProjectId(projectId).stream()
                .map(b -> new BusinessRuleDto(b.getRuleId(), b.getRuleDescription()))
                .collect(Collectors.toList());
    }

    public List<UsecaseDto> getUsecases(Integer projectId) {
        return usecaseRepository.findByProject_ProjectId(projectId).stream()
                .map(u -> new UsecaseDto(u.getUsecaseId(), u.getUsecaseName(), u.getPrecondition(), u.getPostcondition(), u.getExceptions(), u.getPriority()))
                .collect(Collectors.toList());
    }

    public List<FunctionalReqDto> getFunctionalRequirements(Integer projectId) {
        return functionalRequirementRepository.findByProject_ProjectId(projectId).stream()
                .map(f -> new FunctionalReqDto(f.getRequirementId(), f.getTitle(), f.getDescription()))
                .collect(Collectors.toList());
    }

    public List<NonFunctionalReqDto> getNonFunctionalRequirements(Integer projectId) {
        return nonFunctionalRequirementRepository.findByProject_ProjectId(projectId).stream()
                .map(n -> new NonFunctionalReqDto(n.getRequirementId(), n.getCategory(), n.getDescription()))
                .collect(Collectors.toList());
    }
}
