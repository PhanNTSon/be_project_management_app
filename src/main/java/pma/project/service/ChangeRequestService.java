package pma.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.exception.CustomException.EntityNotFoundException;
import pma.common.exception.CustomException.ChangeRequestNotFoundException;
import pma.common.exception.CustomException.OperationNotSupportedException;
import pma.common.mapper.ChangeRequestMapper;
import pma.project.dto.request.ChangeItemSubmitDto;
import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.dto.response.ResponseChangeRequestDto;
import pma.project.entity.change.ChangeItem;
import pma.project.entity.change.ChangeRequest;
import pma.project.entity.core.*;
import pma.project.entity.usecase.Actor;
import pma.project.entity.usecase.BusinessRule;
import pma.project.entity.usecase.Usecase;
import pma.project.repository.*;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeItemRepository changeItemRepository;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectRepository projectRepository;
    private final UserRepo userRepository;
    private final ObjectMapper objectMapper;
    private final ChangeRequestMapper changeRequestMapper;

    // --- Repositories for entity dispatch ---
    private final UsecaseRepository usecaseRepository;
    private final VisionScopeRepository visionScopeRepository;
    private final ConstraintRepository constraintRepository;
    private final FunctionalRequirementRepository functionalRequirementRepository;
    private final NonFunctionalRequirementRepository nonFunctionalRequirementRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final ActorRepository actorRepository;

    // =====================================================================
    // PUBLIC API METHODS
    // =====================================================================

    /**
     * Trả về danh sách ChangeRequest của một Project. Chỉ OWNER/MAINTAINER (role có APPROVE_CHANGE) mới xem được.
     */
    public List<ResponseChangeRequestDto> getChangeRequestsByProject(Long userId, Integer projectId) {
        projectPermissionService.validatePermission(userId, projectId, "APPROVE_CHANGE");
        return changeRequestRepository.findByProject_ProjectId(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Tạo ChangeRequest mới. EDITOR cần quyền EDIT_WITH_REQUEST, MAINTAINER/OWNER cần EDIT_DIRECT.
     * Nếu user có quyền EDIT_DIRECT, request sẽ được auto-approve ngay lập tức.
     */
    @Transactional
    public ChangeRequest createChangeRequest(Long userId, Integer projectId, ChangeRequestSubmitDto dto) {
        // Phải có ít nhất một trong hai quyền chỉnh sửa
        boolean canEditDirect = projectPermissionService.hasPermission(userId, projectId, "EDIT_DIRECT");
        boolean canEditWithRequest = projectPermissionService.hasPermission(userId, projectId, "EDIT_WITH_REQUEST");

        if (!canEditDirect && !canEditWithRequest) {
            // Gọi validatePermission để ném ForbiddenAccessException với message rõ ràng
            projectPermissionService.validatePermission(userId, projectId, "EDIT_WITH_REQUEST");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Tạo PENDING ChangeRequest
        ChangeRequest request = new ChangeRequest(project, requester, dto.getTitle(), dto.getDescription());
        request = changeRequestRepository.save(request);

        // Lưu từng ChangeItem
        for (ChangeItemSubmitDto itemDto : dto.getItems()) {
            ChangeItem item = new ChangeItem(request, itemDto.getEntityType(), itemDto.getOperation());
            item.setEntityId(itemDto.getEntityId());
            item.setFieldName(itemDto.getFieldName());
            item.setOldValue(itemDto.getOldValue());
            item.setNewValue(itemDto.getNewValue());
            changeItemRepository.save(item);
        }

        // Auto-approve nếu có quyền EDIT_DIRECT (MAINTAINER/OWNER)
        if (canEditDirect) {
            return applyChangeRequest(userId, request.getChangeRequestId());
        }

        return request;
    }

    /**
     * Maintainer/Owner phê duyệt ChangeRequest: áp dụng thay đổi vào các entity gốc.
     */
    @Transactional
    public ChangeRequest applyChangeRequest(Long reviewerId, Integer requestId) {
        ChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChangeRequestNotFoundException(requestId));

        projectPermissionService.validatePermission(reviewerId, request.getProject().getProjectId(), "APPROVE_CHANGE");

        // Lặp qua từng ChangeItem và dispatch sang handler tương ứng
        List<ChangeItem> items = changeItemRepository.findByChangeRequest_ChangeRequestId(requestId);
        for (ChangeItem item : items) {
            dispatchChangeItem(item, request.getProject());
        }

        // Cập nhật trạng thái APPROVED
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        request.setStatus("APPROVED");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        return changeRequestRepository.save(request);
    }

    /**
     * Maintainer/Owner từ chối ChangeRequest.
     */
    @Transactional
    public ChangeRequest rejectChangeRequest(Long reviewerId, Integer requestId) {
        ChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChangeRequestNotFoundException(requestId));

        projectPermissionService.validatePermission(reviewerId, request.getProject().getProjectId(), "APPROVE_CHANGE");

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        request.setStatus("REJECTED");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        return changeRequestRepository.save(request);
    }

    // =====================================================================
    // PRIVATE: DISPATCH LOGIC
    // =====================================================================

    private void dispatchChangeItem(ChangeItem item, Project project) {
        switch (item.getEntityType().toUpperCase()) {
            case "USECASE"            -> applyUsecaseChange(item, project);
            case "VISION_SCOPE"       -> applyVisionScopeChange(item, project);
            case "CONSTRAINT"         -> applyConstraintChange(item, project);
            case "FUNCTIONAL_REQ"     -> applyFunctionalReqChange(item, project);
            case "NON_FUNCTIONAL_REQ" -> applyNonFunctionalReqChange(item, project);
            case "BUSINESS_RULE"      -> applyBusinessRuleChange(item, project);
            case "ACTOR"              -> applyActorChange(item, project);
            case "PROJECT"            -> applyProjectChange(item);
            default -> throw new IllegalArgumentException("Unknown entity type: " + item.getEntityType());
        }
    }

    // ---- USECASE ----
    private void applyUsecaseChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                Usecase usecase = usecaseRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Usecase", item.getEntityId()));
                if (values.containsKey("usecaseName"))  usecase.setUsecaseName((String) values.get("usecaseName"));
                if (values.containsKey("precondition")) usecase.setPrecondition((String) values.get("precondition"));
                if (values.containsKey("postcondition")) usecase.setPostcondition((String) values.get("postcondition"));
                if (values.containsKey("exceptions"))   usecase.setExceptions((String) values.get("exceptions"));
                if (values.containsKey("priority"))     usecase.setPriority((String) values.get("priority"));
                usecaseRepository.save(usecase);
            }
            case "DELETE" -> usecaseRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                // CREATE Usecase cần FunctionalRequirement và CreatedBy được truyền qua newValue
                // Ví dụ newValue: {"usecaseName":"...", "functionalRequirementId": 1, "createdById": 2, ...}
                // Để đơn giản, phần này cần extend thêm nếu có use case thực tế
                throw new OperationNotSupportedException("CREATE", "USECASE");
            }
        }
    }

    // ---- VISION SCOPE ----
    private void applyVisionScopeChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                VisionScope vs = visionScopeRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("VisionScope", item.getEntityId()));
                if (values.containsKey("content")) vs.setContent((String) values.get("content"));
                visionScopeRepository.save(vs);
            }
            case "DELETE" -> visionScopeRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                VisionScope vs = new VisionScope();
                vs.setProject(project);
                vs.setContent((String) values.get("content"));
                visionScopeRepository.save(vs);
            }
        }
    }

    // ---- CONSTRAINT ----
    private void applyConstraintChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                Constraint c = constraintRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Constraint", item.getEntityId()));
                if (values.containsKey("type"))        c.setType((String) values.get("type"));
                if (values.containsKey("description")) c.setDescription((String) values.get("description"));
                constraintRepository.save(c);
            }
            case "DELETE" -> constraintRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                Constraint c = new Constraint();
                c.setProject(project);
                c.setType((String) values.get("type"));
                c.setDescription((String) values.get("description"));
                constraintRepository.save(c);
            }
        }
    }

    // ---- FUNCTIONAL REQUIREMENT ----
    private void applyFunctionalReqChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                FunctionalRequirement req = functionalRequirementRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", item.getEntityId()));
                if (values.containsKey("title"))       req.setTitle((String) values.get("title"));
                if (values.containsKey("description")) req.setDescription((String) values.get("description"));
                functionalRequirementRepository.save(req);
            }
            case "DELETE" -> functionalRequirementRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                FunctionalRequirement req = new FunctionalRequirement();
                req.setProject(project);
                req.setTitle((String) values.get("title"));
                req.setDescription((String) values.get("description"));
                functionalRequirementRepository.save(req);
            }
        }
    }

    // ---- NON FUNCTIONAL REQUIREMENT ----
    private void applyNonFunctionalReqChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                NonFunctionalRequirement req = nonFunctionalRequirementRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("NonFunctionalRequirement", item.getEntityId()));
                if (values.containsKey("category"))    req.setCategory((String) values.get("category"));
                if (values.containsKey("description")) req.setDescription((String) values.get("description"));
                nonFunctionalRequirementRepository.save(req);
            }
            case "DELETE" -> nonFunctionalRequirementRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                NonFunctionalRequirement req = new NonFunctionalRequirement();
                req.setProject(project);
                req.setCategory((String) values.get("category"));
                req.setDescription((String) values.get("description"));
                nonFunctionalRequirementRepository.save(req);
            }
        }
    }

    // ---- BUSINESS RULE ----
    private void applyBusinessRuleChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                BusinessRule rule = businessRuleRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("BusinessRule", item.getEntityId()));
                if (values.containsKey("ruleDescription")) rule.setRuleDescription((String) values.get("ruleDescription"));
                businessRuleRepository.save(rule);
            }
            case "DELETE" -> businessRuleRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                BusinessRule rule = new BusinessRule();
                rule.setProject(project);
                rule.setRuleDescription((String) values.get("ruleDescription"));
                businessRuleRepository.save(rule);
            }
        }
    }

    // ---- ACTOR ----
    private void applyActorChange(ChangeItem item, Project project) {
        Map<String, Object> values = parseJson(item.getNewValue());
        switch (item.getOperation()) {
            case "UPDATE" -> {
                Actor actor = actorRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Actor", item.getEntityId()));
                if (values.containsKey("actorName"))   actor.setActorName((String) values.get("actorName"));
                if (values.containsKey("description")) actor.setDescription((String) values.get("description"));
                actorRepository.save(actor);
            }
            case "DELETE" -> actorRepository.deleteById(item.getEntityId());
            case "CREATE" -> {
                Actor actor = new Actor();
                actor.setProject(project);
                actor.setActorName((String) values.get("actorName"));
                actor.setDescription((String) values.get("description"));
                actorRepository.save(actor);
            }
        }
    }

    // ---- PROJECT ----
    private void applyProjectChange(ChangeItem item) {
        Map<String, Object> values = parseJson(item.getNewValue());
        Project project = projectRepository.findById(item.getEntityId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + item.getEntityId()));
        if (values.containsKey("projectName")) project.setProjectName((String) values.get("projectName"));
        if (values.containsKey("description")) project.setDescription((String) values.get("description"));
        projectRepository.save(project);
    }

    // =====================================================================
    // PRIVATE: UTILITIES
    // =====================================================================

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in ChangeItem.newValue: " + json, e);
        }
    }

    private ResponseChangeRequestDto toDto(ChangeRequest request) {
        List<ChangeItem> items = changeItemRepository.findByChangeRequest_ChangeRequestId(request.getChangeRequestId());
        ResponseChangeRequestDto dto = changeRequestMapper.toDto(request);
        dto.setItems(items.stream().map(changeRequestMapper::itemToDto).collect(Collectors.toList()));
        return dto;
    }
}
