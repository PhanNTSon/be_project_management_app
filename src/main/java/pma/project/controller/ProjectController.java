package pma.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<List<ResponseProjectListDto>> getMyProjects(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getProjectsByUserId(userId));
    }

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
    public ResponseEntity<List<ResponsePermissionDto>> getPermissions(@PathVariable Integer projectId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(projectService.getPermissions(projectId, userId));
    }
}


