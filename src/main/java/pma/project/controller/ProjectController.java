package pma.project.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import pma.project.dto.request.RequestCreateProjectDto;
import pma.project.dto.response.*;
import pma.project.service.ProjectService;

import pma.user.entity.User;
import pma.user.repository.UserRepo;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepo userRepository;

    private Long getCurrentUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getUserId();
    }

    // =====================================================================
    // PROJECT CRUD
    // =====================================================================

    @GetMapping
    public ResponseEntity<List<ResponseProjectListDto>> getMyProjects(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getProjectsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ResponseProjectListDto> createProject(
            @Valid @RequestBody RequestCreateProjectDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(userId, dto));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        projectService.deleteProject(userId, projectId);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // PROJECT DATA QUERIES
    // =====================================================================

    @GetMapping("/{projectId}/context-diagram")
    public ResponseEntity<String> getContextDiagramUrl(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getContextDiagramUrl(projectId));
    }

    @GetMapping("/{projectId}/vision-scopes")
    public ResponseEntity<List<ResponseVisionScopeDto>> getVisionScopes(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getVisionScopes(projectId));
    }

    @GetMapping("/{projectId}/constraints")
    public ResponseEntity<List<ResponseConstraintDto>> getConstraints(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getConstraints(projectId));
    }

    @GetMapping("/{projectId}/business-rules")
    public ResponseEntity<List<ResponseBusinessRuleDto>> getBusinessRules(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getBusinessRules(projectId));
    }

    @GetMapping("/{projectId}/usecases")
    public ResponseEntity<List<ResponseUsecaseDto>> getUsecases(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getUsecases(projectId));
    }

    @GetMapping("/{projectId}/functional-requirements")
    public ResponseEntity<List<ResponseFunctionalReqDto>> getFunctionalRequirements(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getFunctionalRequirements(projectId));
    }

    @GetMapping("/{projectId}/non-functional-requirements")
    public ResponseEntity<List<ResponseNonFunctionalReqDto>> getNonFunctionalRequirements(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.getNonFunctionalRequirements(projectId));
    }

    @GetMapping("/{projectId}/permissions")
    public ResponseEntity<List<ResponsePermissionDto>> getPermissions(
            @PathVariable Integer projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getPermissions(projectId, userId));
    }
}
