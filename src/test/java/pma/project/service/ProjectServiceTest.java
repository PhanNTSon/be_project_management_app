package pma.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.project.dto.response.*;
import pma.project.entity.core.*;
import pma.project.entity.usecase.*;
import pma.project.repository.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private VisionScopeRepository visionScopeRepository;
    @Mock
    private ConstraintRepository constraintRepository;
    @Mock
    private BusinessRuleRepository businessRuleRepository;
    @Mock
    private UsecaseRepository usecaseRepository;
    @Mock
    private FunctionalRequirementRepository functionalRequirementRepository;
    @Mock
    private NonFunctionalRequirementRepository nonFunctionalRequirementRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getProjectsByUserId_ShouldReturnProjectList() {
        Long userId = 1L;
        Project p1 = new Project();
        p1.setProjectName("Project 1");
        p1.setDescription("Desc 1");

        Project p2 = new Project();
        p2.setProjectName("Project 2");
        p2.setDescription("Desc 2");

        when(projectRepository.findProjectsByUserId(userId)).thenReturn(Arrays.asList(p1, p2));

        List<ResponseProjectListDto> result = projectService.getProjectsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getProjectName());
        assertEquals("Project 2", result.get(1).getProjectName());
    }

    @Test
    void getContextDiagramUrl_ProjectExists_ShouldReturnUrl() {
        Integer projectId = 1;
        Project project = new Project();
        project.setContextDiagramUrl("http://example.com/diagram.png");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        String url = projectService.getContextDiagramUrl(projectId);

        assertEquals("http://example.com/diagram.png", url);
    }

    @Test
    void getContextDiagramUrl_ProjectNotFound_ShouldThrowException() {
        Integer projectId = 99;
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getContextDiagramUrl(projectId));
    }

    @Test
    void getVisionScopes_ShouldReturnDtoList() {
        Integer projectId = 1;
        VisionScope v = new VisionScope();
        v.setContent("Scope Content");
        
        when(visionScopeRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(v));

        List<ResponseVisionScopeDto> result = projectService.getVisionScopes(projectId);

        assertEquals(1, result.size());
        assertEquals("Scope Content", result.get(0).getContent());
    }

    @Test
    void getConstraints_ShouldReturnDtoList() {
        Integer projectId = 1;
        Constraint c = new Constraint();
        c.setDescription("Constraint Desc");
        
        when(constraintRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(c));

        List<ResponseConstraintDto> result = projectService.getConstraints(projectId);

        assertEquals(1, result.size());
        assertEquals("Constraint Desc", result.get(0).getDescription());
    }

    @Test
    void getBusinessRules_ShouldReturnDtoList() {
        Integer projectId = 1;
        BusinessRule b = new BusinessRule();
        b.setRuleDescription("BR Desc");
        
        when(businessRuleRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(b));

        List<ResponseBusinessRuleDto> result = projectService.getBusinessRules(projectId);

        assertEquals(1, result.size());
        assertEquals("BR Desc", result.get(0).getRuleDescription());
    }

    @Test
    void getUsecases_ShouldReturnDtoList() {
        Integer projectId = 1;
        Usecase u = new Usecase();
        u.setUsecaseName("UC Name");
        u.setPriority("HIGH");
        
        when(usecaseRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(u));

        List<ResponseUsecaseDto> result = projectService.getUsecases(projectId);

        assertEquals(1, result.size());
        assertEquals("UC Name", result.get(0).getUsecaseName());
        assertEquals("HIGH", result.get(0).getPriority());
    }

    @Test
    void getFunctionalRequirements_ShouldReturnDtoList() {
        Integer projectId = 1;
        FunctionalRequirement f = new FunctionalRequirement();
        f.setTitle("FR Title");
        f.setDescription("FR Desc");
        
        when(functionalRequirementRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(f));

        List<ResponseFunctionalReqDto> result = projectService.getFunctionalRequirements(projectId);

        assertEquals(1, result.size());
        assertEquals("FR Title", result.get(0).getTitle());
        assertEquals("FR Desc", result.get(0).getDescription());
    }

    @Test
    void getNonFunctionalRequirements_ShouldReturnDtoList() {
        Integer projectId = 1;
        NonFunctionalRequirement n = new NonFunctionalRequirement();
        n.setCategory("PERFORMANCE");
        n.setDescription("NFR Desc");
        
        when(nonFunctionalRequirementRepository.findByProject_ProjectId(projectId)).thenReturn(Arrays.asList(n));

        List<ResponseNonFunctionalReqDto> result = projectService.getNonFunctionalRequirements(projectId);

        assertEquals(1, result.size());
        assertEquals("PERFORMANCE", result.get(0).getCategory());
        assertEquals("NFR Desc", result.get(0).getDescription());
    }

    @Test
    void getPermissions_ShouldReturnDtoList() {
        Integer projectId = 1;
        Long userId = 1L;
        pma.project.entity.member.ProjectMember member = new pma.project.entity.member.ProjectMember();
        pma.project.entity.member.ProjectRole role = new pma.project.entity.member.ProjectRole("Role", "Desc");
        pma.project.entity.member.Permission permission = new pma.project.entity.member.Permission("CODE", "Desc");
        role.addPermission(permission);
        member.setProjectRole(role);
        
        when(projectMemberRepository.findById(new pma.project.entity.member.ProjectMemberId(projectId, userId))).thenReturn(Optional.of(member));

        List<ResponsePermissionDto> result = projectService.getPermissions(projectId, userId);

        assertEquals(1, result.size());
        assertEquals("CODE", result.get(0).getCode());
        assertEquals("Desc", result.get(0).getDescription());
    }

    @Test
    void getPermissions_ProjectMemberNotFound_ShouldThrowException() {
        Integer projectId = 99;
        Long userId = 1L;
        
        when(projectMemberRepository.findById(new pma.project.entity.member.ProjectMemberId(projectId, userId))).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getPermissions(projectId, userId));
    }
}

