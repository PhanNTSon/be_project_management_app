package pma.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pma.common.exception.CustomException.ProjectNotFoundException;
import pma.common.exception.CustomException.UserNotFoundException;
import pma.common.exception.CustomException.EntityNotFoundException;
import pma.common.exception.CustomException.ChangeRequestNotFoundException;
import pma.common.mapper.*;
import pma.project.dto.change.*;
import pma.project.dto.request.ChangeItemSubmitDto;
import pma.project.dto.request.ChangeRequestSubmitDto;
import pma.project.dto.response.ResponseChangeRequestDto;
import pma.project.entity.change.ChangeItem;
import pma.project.entity.change.ChangeRequest;
import pma.project.entity.core.*;
import pma.project.entity.usecase.*;
import pma.project.repository.*;
import pma.user.entity.User;
import pma.user.repository.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeItemRepository changeItemRepository;
    private final ProjectPermissionService projectPermissionService;
    private final ProjectRepository projectRepository;
    private final UserRepo userRepository;
    private final ObjectMapper objectMapper;
    private final ChangeRequestMapper changeRequestMapper;

    // --- DTO Mappers for entity conversion ---
    private final UsecaseMapper usecaseMapper;
    private final VisionScopeMapper visionScopeMapper;
    private final ConstraintMapper constraintMapper;
    private final BusinessRuleMapper businessRuleMapper;
    private final FunctionalReqMapper functionalReqMapper;
    private final NonFunctionalReqMapper nonFunctionalReqMapper;

    // --- Repositories for entity dispatch ---
    private final UsecaseRepository usecaseRepository;
    private final VisionScopeRepository visionScopeRepository;
    private final ConstraintRepository constraintRepository;
    private final FunctionalRequirementRepository functionalRequirementRepository;
    private final NonFunctionalRequirementRepository nonFunctionalRequirementRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final ActorRepository actorRepository;
    private final UsecaseFlowRepository usecaseFlowRepository;
    private final UsecaseBusinessRuleRepository usecaseBusinessRuleRepository;

    // =====================================================================
    // PUBLIC API METHODS
    // =====================================================================

    public List<ResponseChangeRequestDto> getChangeRequestsByProject(Long userId, Integer projectId) {
        projectPermissionService.validatePermission(userId, projectId, "APPROVE_CHANGE");
        return changeRequestRepository.findByProject_ProjectId(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChangeRequest createChangeRequest(Long userId, Integer projectId, ChangeRequestSubmitDto dto) {
        boolean canEditDirect = projectPermissionService.hasPermission(userId, projectId, "EDIT_DIRECT");
        boolean canEditWithRequest = projectPermissionService.hasPermission(userId, projectId, "EDIT_WITH_REQUEST");

        if (!canEditDirect && !canEditWithRequest) {
            projectPermissionService.validatePermission(userId, projectId, "EDIT_WITH_REQUEST");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ChangeRequest request = new ChangeRequest(project, requester, dto.getTitle(), dto.getDescription());
        request = changeRequestRepository.save(request);

        for (ChangeItemSubmitDto itemDto : dto.getItems()) {
            ChangeItem item = new ChangeItem(request, itemDto.getEntityType(), itemDto.getOperation());
            item.setEntityId(itemDto.getEntityId());
            item.setFieldName(itemDto.getFieldName());
            item.setOldValue(itemDto.getOldValue());
            item.setNewValue(itemDto.getNewValue());
            changeItemRepository.save(item);
        }

        if (canEditDirect) {
            return applyChangeRequest(userId, request.getChangeRequestId());
        }

        return request;
    }

    @Transactional
    public ChangeRequest applyChangeRequest(Long reviewerId, Integer requestId) {
        ChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChangeRequestNotFoundException(requestId));

        projectPermissionService.validatePermission(reviewerId, request.getProject().getProjectId(), "APPROVE_CHANGE");

        List<ChangeItem> items = changeItemRepository.findByChangeRequest_ChangeRequestId(requestId);
        for (ChangeItem item : items) {
            validateChangeItem(item);
            dispatchChangeItem(item, request.getProject(), request.getRequester());
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        request.setStatus("APPROVED");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        return changeRequestRepository.save(request);
    }

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
    // PHASE 8 - VALIDATION
    // =====================================================================

    private void validateChangeItem(ChangeItem item) {
        log.info("Validating ChangeItem - entityType={}, operation={}, entityId={}",
                item.getEntityType(), item.getOperation(), item.getEntityId());

        if ("CREATE".equals(item.getOperation())) {
            if (item.getEntityId() != null) {
                throw new IllegalStateException("CREATE operation must have entityId = null");
            }
        } else if ("UPDATE".equals(item.getOperation()) || "DELETE".equals(item.getOperation())) {
            if (item.getEntityId() == null) {
                throw new IllegalStateException(item.getOperation() + " operation must have a valid entityId");
            }
        } else {
            throw new IllegalStateException("Invalid operation: " + item.getOperation());
        }
    }

    // =====================================================================
    // PRIVATE: DISPATCH LOGIC
    // =====================================================================

    private void dispatchChangeItem(ChangeItem item, Project project, User createdByUser) {
        log.info("🔷 Applying change: entityType={}, operation={}, entityId={}",
                item.getEntityType(), item.getOperation(), item.getEntityId());
        try {
            switch (item.getEntityType().toUpperCase()) {
                case "USECASE"            -> applyUsecaseChange(item, project, createdByUser);
                case "VISION_SCOPE"       -> applyVisionScopeChange(item, project);
                case "CONSTRAINT"         -> applyConstraintChange(item, project);
                case "FUNCTIONAL_REQ"     -> applyFunctionalReqChange(item, project);
                case "NON_FUNCTIONAL_REQ" -> applyNonFunctionalReqChange(item, project);
                case "BUSINESS_RULE"      -> applyBusinessRuleChange(item, project);
                case "ACTOR"              -> applyActorChange(item, project);
                case "PROJECT"            -> applyProjectChange(item);
                default -> throw new IllegalArgumentException("Unknown entity type: " + item.getEntityType());
            }
            log.info("✅ Successfully processed change item");
        } catch (Exception e) {
            log.error("❌ Error processing change item: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to apply change: " + e.getMessage(), e);
        }
    }

    // ---- USECASE ----
    private void applyUsecaseChange(ChangeItem item, Project project, User createdByUser) {
        switch (item.getOperation()) {
            case "CREATE" -> createUsecase(item, project, createdByUser);
            case "UPDATE" -> updateUsecase(item, project);
            case "DELETE" -> deleteUsecase(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createUsecase(ChangeItem item, Project project, User createdByUser) {
        if (createdByUser == null) {
            throw new IllegalArgumentException("CREATE operation requires createdBy user");
        }

        log.info("📝 CREATE USECASE: Building usecase from payload");

        UsecasePayloadDto dto = parseJson(item.getNewValue(), UsecasePayloadDto.class);
        Usecase usecase = usecaseMapper.toEntity(dto);
        usecase.setProject(project);
        usecase.setCreatedBy(createdByUser);

        // Validate and set FunctionalRequirement
        if (dto.getFunctionRelId() != null) {
            FunctionalRequirement fr = functionalRequirementRepository.findById(dto.getFunctionRelId())
                    .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", dto.getFunctionRelId()));
            usecase.setFunctionalRequirement(fr);
        } else {
            FunctionalRequirement firstFr = functionalRequirementRepository.findByProject_ProjectId(project.getProjectId())
                    .stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No FunctionalRequirement available in project"));
            usecase.setFunctionalRequirement(firstFr);
        }

        usecaseRepository.save(usecase);
        log.info("✅ Usecase created with ID: {}", usecase.getUsecaseId());

        createUsecaseRelatedEntities(usecase, dto);
    }

    private void updateUsecase(ChangeItem item, Project project) {
        log.info("✏️ UPDATE USECASE: Updating usecase {}", item.getEntityId());

        Usecase usecase = usecaseRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("Usecase", item.getEntityId()));

        UsecasePayloadDto dto = parseJson(item.getNewValue(), UsecasePayloadDto.class);
        usecaseMapper.updateEntity(dto, usecase);

        if (dto.getFunctionRelId() != null) {
            FunctionalRequirement fr = functionalRequirementRepository.findById(dto.getFunctionRelId())
                    .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", dto.getFunctionRelId()));
            usecase.setFunctionalRequirement(fr);
        }

        usecaseRepository.save(usecase);
        log.info("✅ Usecase updated successfully");

        updateUsecaseRelatedEntities(usecase, dto);
    }

    private void deleteUsecase(ChangeItem item) {
        log.info("🗑️ DELETE USECASE: Deleting usecase {}", item.getEntityId());
        usecaseRepository.deleteById(item.getEntityId());
        log.info("✅ Usecase deleted successfully");
    }

    private void createUsecaseRelatedEntities(Usecase usecase, UsecasePayloadDto dto) {
        // Create flows
        if (dto.getNormalFlows() != null) {
            for (String flowDesc : dto.getNormalFlows()) {
                try {
                    UsecaseFlow flow = new UsecaseFlow();
                    flow.setUsecase(usecase);
                    flow.setFlowType("NORMAL");
                    flow.setDescription(flowDesc);
                    flow.setAlternative(false);
                    usecaseFlowRepository.save(flow);
                    log.debug("✅ Created NORMAL flow");
                } catch (Exception e) {
                    log.warn("⚠️ Failed to create normal flow: {}", e.getMessage());
                }
            }
        }

        if (dto.getAlterFlows() != null) {
            for (String flowDesc : dto.getAlterFlows()) {
                try {
                    UsecaseFlow flow = new UsecaseFlow();
                    flow.setUsecase(usecase);
                    flow.setFlowType("ALTERNATIVE");
                    flow.setDescription(flowDesc);
                    flow.setAlternative(true);
                    usecaseFlowRepository.save(flow);
                    log.debug("✅ Created ALTERNATIVE flow");
                } catch (Exception e) {
                    log.warn("⚠️ Failed to create alternative flow: {}", e.getMessage());
                }
            }
        }

        // Create business rule links
        if (dto.getLinkedBusinessRuleIds() != null) {
            for (Integer brId : dto.getLinkedBusinessRuleIds()) {
                try {
                    BusinessRule br = businessRuleRepository.findById(brId)
                            .orElseThrow(() -> new EntityNotFoundException("BusinessRule", brId));
                    UsecaseBusinessRule ubr = new UsecaseBusinessRule(usecase, br);
                    usecaseBusinessRuleRepository.save(ubr);
                    log.debug("✅ Linked BusinessRule {}", brId);
                } catch (Exception e) {
                    log.warn("⚠️ Failed to link BusinessRule: {}", e.getMessage());
                }
            }
        }
    }

    private void updateUsecaseRelatedEntities(Usecase usecase, UsecasePayloadDto dto) {
        log.info("🔄 Updating usecase related entities for usecase ID: {}", usecase.getUsecaseId());

        // PHASE 6: Delete old flows and business rule links before recreating
        try {
            usecaseFlowRepository.deleteByUsecase_UsecaseId(usecase.getUsecaseId());
            usecaseBusinessRuleRepository.deleteByUsecase_UsecaseId(usecase.getUsecaseId());
            log.info("✅ Deleted old flows and business rule links");
        } catch (Exception e) {
            log.warn("⚠️ Error deleting old relationships: {}", e.getMessage());
        }

        // Recreate all relationships
        createUsecaseRelatedEntities(usecase, dto);
        log.info("✅ Usecase related entities updated");
    }

    // ---- VISION SCOPE ----
    private void applyVisionScopeChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> createVisionScope(item, project);
            case "UPDATE" -> updateVisionScope(item);
            case "DELETE" -> deleteVisionScope(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createVisionScope(ChangeItem item, Project project) {
        log.info("📝 CREATE VISION_SCOPE");
        VisionScopePayloadDto dto = parseJson(item.getNewValue(), VisionScopePayloadDto.class);
        VisionScope vs = visionScopeMapper.toEntity(dto);
        vs.setProject(project);
        visionScopeRepository.save(vs);
        log.info("✅ VisionScope created successfully");
    }

    private void updateVisionScope(ChangeItem item) {
        log.info("✏️ UPDATE VISION_SCOPE: {}", item.getEntityId());
        VisionScope vs = visionScopeRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("VisionScope", item.getEntityId()));
        VisionScopePayloadDto dto = parseJson(item.getNewValue(), VisionScopePayloadDto.class);
        visionScopeMapper.updateEntity(dto, vs);
        visionScopeRepository.save(vs);
        log.info("✅ VisionScope updated successfully");
    }

    private void deleteVisionScope(ChangeItem item) {
        log.info("🗑️ DELETE VISION_SCOPE: {}", item.getEntityId());
        visionScopeRepository.deleteById(item.getEntityId());
        log.info("✅ VisionScope deleted successfully");
    }

    // ---- CONSTRAINT ----
    private void applyConstraintChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> createConstraint(item, project);
            case "UPDATE" -> updateConstraint(item);
            case "DELETE" -> deleteConstraint(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createConstraint(ChangeItem item, Project project) {
        log.info("📝 CREATE CONSTRAINT");
        ConstraintPayloadDto dto = parseJson(item.getNewValue(), ConstraintPayloadDto.class);
        Constraint c = constraintMapper.toEntity(dto);
        c.setProject(project);
        constraintRepository.save(c);
        log.info("✅ Constraint created successfully");
    }

    private void updateConstraint(ChangeItem item) {
        log.info("✏️ UPDATE CONSTRAINT: {}", item.getEntityId());
        Constraint c = constraintRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("Constraint", item.getEntityId()));
        ConstraintPayloadDto dto = parseJson(item.getNewValue(), ConstraintPayloadDto.class);
        constraintMapper.updateEntity(dto, c);
        constraintRepository.save(c);
        log.info("✅ Constraint updated successfully");
    }

    private void deleteConstraint(ChangeItem item) {
        log.info("🗑️ DELETE CONSTRAINT: {}", item.getEntityId());
        constraintRepository.deleteById(item.getEntityId());
        log.info("✅ Constraint deleted successfully");
    }

    // ---- FUNCTIONAL REQUIREMENT ----
    private void applyFunctionalReqChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> createFunctionalReq(item, project);
            case "UPDATE" -> updateFunctionalReq(item);
            case "DELETE" -> deleteFunctionalReq(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createFunctionalReq(ChangeItem item, Project project) {
        log.info("📝 CREATE FUNCTIONAL_REQ");
        FunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), FunctionalRequirementPayloadDto.class);
        FunctionalRequirement req = functionalReqMapper.toEntity(dto);
        req.setProject(project);
        functionalRequirementRepository.save(req);
        log.info("✅ FunctionalRequirement created successfully");
    }

    private void updateFunctionalReq(ChangeItem item) {
        log.info("✏️ UPDATE FUNCTIONAL_REQ: {}", item.getEntityId());
        FunctionalRequirement req = functionalRequirementRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", item.getEntityId()));
        FunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), FunctionalRequirementPayloadDto.class);
        functionalReqMapper.updateEntity(dto, req);
        functionalRequirementRepository.save(req);
        log.info("✅ FunctionalRequirement updated successfully");
    }

    private void deleteFunctionalReq(ChangeItem item) {
        log.info("🗑️ DELETE FUNCTIONAL_REQ: {}", item.getEntityId());
        functionalRequirementRepository.deleteById(item.getEntityId());
        log.info("✅ FunctionalRequirement deleted successfully");
    }

    // ---- NON FUNCTIONAL REQUIREMENT ----
    private void applyNonFunctionalReqChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> createNonFunctionalReq(item, project);
            case "UPDATE" -> updateNonFunctionalReq(item);
            case "DELETE" -> deleteNonFunctionalReq(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createNonFunctionalReq(ChangeItem item, Project project) {
        log.info("📝 CREATE NON_FUNCTIONAL_REQ");
        NonFunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), NonFunctionalRequirementPayloadDto.class);
        NonFunctionalRequirement req = nonFunctionalReqMapper.toEntity(dto);
        req.setProject(project);
        nonFunctionalRequirementRepository.save(req);
        log.info("✅ NonFunctionalRequirement created successfully");
    }

    private void updateNonFunctionalReq(ChangeItem item) {
        log.info("✏️ UPDATE NON_FUNCTIONAL_REQ: {}", item.getEntityId());
        NonFunctionalRequirement req = nonFunctionalRequirementRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("NonFunctionalRequirement", item.getEntityId()));
        NonFunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), NonFunctionalRequirementPayloadDto.class);
        nonFunctionalReqMapper.updateEntity(dto, req);
        nonFunctionalRequirementRepository.save(req);
        log.info("✅ NonFunctionalRequirement updated successfully");
    }

    private void deleteNonFunctionalReq(ChangeItem item) {
        log.info("🗑️ DELETE NON_FUNCTIONAL_REQ: {}", item.getEntityId());
        nonFunctionalRequirementRepository.deleteById(item.getEntityId());
        log.info("✅ NonFunctionalRequirement deleted successfully");
    }

    // ---- BUSINESS RULE ----
    private void applyBusinessRuleChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> createBusinessRule(item, project);
            case "UPDATE" -> updateBusinessRule(item);
            case "DELETE" -> deleteBusinessRule(item);
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    private void createBusinessRule(ChangeItem item, Project project) {
        log.info("📝 CREATE BUSINESS_RULE");
        BusinessRulePayloadDto dto = parseJson(item.getNewValue(), BusinessRulePayloadDto.class);
        BusinessRule rule = businessRuleMapper.toEntity(dto);
        rule.setProject(project);
        businessRuleRepository.save(rule);
        log.info("✅ BusinessRule created successfully");
    }

    private void updateBusinessRule(ChangeItem item) {
        log.info("✏️ UPDATE BUSINESS_RULE: {}", item.getEntityId());
        BusinessRule rule = businessRuleRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("BusinessRule", item.getEntityId()));
        BusinessRulePayloadDto dto = parseJson(item.getNewValue(), BusinessRulePayloadDto.class);
        businessRuleMapper.updateEntity(dto, rule);
        businessRuleRepository.save(rule);
        log.info("✅ BusinessRule updated successfully");
    }

    private void deleteBusinessRule(ChangeItem item) {
        log.info("🗑️ DELETE BUSINESS_RULE: {}", item.getEntityId());
        businessRuleRepository.deleteById(item.getEntityId());
        log.info("✅ BusinessRule deleted successfully");
    }

    // ---- ACTOR ----
    private void applyActorChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> {
                Actor actor = new Actor();
                actor.setProject(project);
                actor.setActorName(item.getFieldName());
                actorRepository.save(actor);
                log.info("✅ Actor created successfully");
            }
            case "UPDATE" -> {
                Actor actor = actorRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Actor", item.getEntityId()));
                actor.setActorName(item.getFieldName());
                actorRepository.save(actor);
                log.info("✅ Actor updated successfully");
            }
            case "DELETE" -> {
                actorRepository.deleteById(item.getEntityId());
                log.info("✅ Actor deleted successfully");
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    // ---- PROJECT ----
    private void applyProjectChange(ChangeItem item) {
        switch (item.getOperation()) {
            case "UPDATE" -> {
                Project project = projectRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + item.getEntityId()));
                log.info("✏️ UPDATE PROJECT: {}", item.getEntityId());
                projectRepository.save(project);
                log.info("✅ Project updated successfully");
            }
            case "DELETE" -> {
                projectRepository.deleteById(item.getEntityId());
                log.info("✅ Project deleted successfully");
            }
            case "CREATE" -> {
                log.warn("⚠️ CREATE PROJECT: This operation is not typically allowed via ChangeRequest");
                throw new IllegalArgumentException("Cannot create Project via ChangeRequest");
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + item.getOperation());
        }
    }

    // =====================================================================
    // PRIVATE: UTILITIES
    // =====================================================================

    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON in ChangeItem payload: " + json, e);
        }
    }

    private ResponseChangeRequestDto toDto(ChangeRequest request) {
        List<ChangeItem> items = changeItemRepository.findByChangeRequest_ChangeRequestId(request.getChangeRequestId());
        ResponseChangeRequestDto dto = changeRequestMapper.toDto(request);
        dto.setItems(items.stream().map(changeRequestMapper::itemToDto).collect(Collectors.toList()));
        return dto;
    }
}
