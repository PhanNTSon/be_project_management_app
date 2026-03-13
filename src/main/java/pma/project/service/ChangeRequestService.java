package pma.project.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import pma.project.repository.ChangeRequestRepository;
import pma.project.repository.ChangeItemRepository;
import pma.project.repository.ProjectRepository;
import pma.user.repository.UserRepo;

import pma.project.entity.change.ChangeRequest;
import pma.project.entity.change.ChangeItem;
import pma.project.entity.core.Project;
import pma.user.entity.User;

import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.dto.request.ChangeItemSubmitDto;

import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.exception.CustomException.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeItemRepository changeItemRepository;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectRepository projectRepository;
    private final UserRepo userRepository;

    @Transactional
    public ChangeRequest createChangeRequest(Long userId, Integer projectId, ChangeRequestSubmitDto dto) {
        // 1. Check Permissions (Any editing role can submit a change request)
        // In a real scenario, you map this to specific DTO action type, but broadly:
        projectPermissionService.validatePermission(userId, projectId, "PROJECT_EDIT"); // Or equivalent base permission
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // 2. Create the PENDING ChangeRequest
        ChangeRequest request = new ChangeRequest(project, requester, dto.getTitle(), dto.getDescription());
        request = changeRequestRepository.save(request);

        // 3. Create ChangeItems
        for (ChangeItemSubmitDto itemDto : dto.getItems()) {
            ChangeItem item = new ChangeItem(request, itemDto.getEntityType(), itemDto.getOperation());
            item.setEntityId(itemDto.getEntityId());
            item.setFieldName(itemDto.getFieldName());
            item.setOldValue(itemDto.getOldValue());
            item.setNewValue(itemDto.getNewValue());
            changeItemRepository.save(item);
        }

        // 4. Auto-approve if user is MAINTAINER
        if (projectPermissionService.hasRole(userId, projectId, "MAINTAINER")) {
            return applyChangeRequest(userId, request.getChangeRequestId());
        }

        return request;
    }

    @Transactional
    public ChangeRequest applyChangeRequest(Long maintainerId, Integer requestId) {
        ChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Change request not found"));

        // 1. Check if user is Maintainer
        projectPermissionService.validatePermission(maintainerId, request.getProject().getProjectId(), "APPROVE_CHANGES");

        // 2. Apply Changes (Reflection or Manual processing per entity type)
        // ... omitted for brevity/implementation depends heavily on Entity structures
        
        // 3. Mark as Approved
        User reviewer = userRepository.findById(maintainerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        request.setStatus("APPROVED");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(java.time.LocalDateTime.now());
        
        return changeRequestRepository.save(request);
    }
}
