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

import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

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
    private final UsecaseActorRepository usecaseActorRepository;
    private final UsecaseDiagramUrlRepository usecaseDiagramUrlRepository;

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
            // Flush to ensure the new ChangeRequest and its ChangeItems are visible
            // within the same transaction before reading them back for apply.
            entityManager.flush();
            return applyChangeRequestInternal(userId, request);
        }

        return request;
    }

    @Transactional
    public ChangeRequest applyChangeRequest(Long reviewerId, Integer requestId) {
        ChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChangeRequestNotFoundException(requestId));

        projectPermissionService.validatePermission(reviewerId, request.getProject().getProjectId(), "APPROVE_CHANGE");

        return applyChangeRequestInternal(reviewerId, request);
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
    // INTERNAL APPLY (shared by auto-approve and explicit approve)
    // =====================================================================

    /**
     * Applies a ChangeRequest by processing all its ChangeItems.
     * Does NOT re-validate permissions — callers are responsible for that.
     */
    private ChangeRequest applyChangeRequestInternal(Long reviewerId, ChangeRequest request) {
        List<ChangeItem> items = changeItemRepository.findByChangeRequest_ChangeRequestId(
                request.getChangeRequestId());

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
        log.info("Applying change: entityType={}, operation={}, entityId={}",
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
            log.info("Successfully processed change item: entityType={}, operation={}",
                    item.getEntityType(), item.getOperation());
        } catch (Exception e) {
            log.error("Error processing change item: entityType={}, operation={}, error={}",
                    item.getEntityType(), item.getOperation(), e.getMessage(), e);
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

        log.info("Creating Usecase from payload");

        UsecasePayloadDto dto = parseJson(item.getNewValue(), UsecasePayloadDto.class);
        Usecase usecase = usecaseMapper.toEntity(dto);
        usecase.setProject(project);
        usecase.setCreatedBy(createdByUser);

        // Validate and set FunctionalRequirement (ignoring empty/temp IDs)
        if (dto.getFunctionRelId() != null && !dto.getFunctionRelId().trim().isEmpty() && !dto.getFunctionRelId().startsWith("temp-")) {
            Integer frId = Integer.valueOf(dto.getFunctionRelId());
            FunctionalRequirement fr = functionalRequirementRepository.findById(frId)
                    .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", frId));
            usecase.setFunctionalRequirement(fr);
        } else {
            FunctionalRequirement firstFr = functionalRequirementRepository.findByProject_ProjectId(project.getProjectId())
                    .stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No FunctionalRequirement available in project"));
            usecase.setFunctionalRequirement(firstFr);
        }

        usecaseRepository.save(usecase);
        log.info("Usecase created with ID: {}", usecase.getUsecaseId());

        createUsecaseRelatedEntities(usecase, dto);
    }

    private void updateUsecase(ChangeItem item, Project project) {
        log.info("Updating Usecase id={}", item.getEntityId());

        Usecase usecase = usecaseRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("Usecase", item.getEntityId()));

        UsecasePayloadDto dto = parseJson(item.getNewValue(), UsecasePayloadDto.class);
        usecaseMapper.updateEntity(dto, usecase);

        if (dto.getFunctionRelId() != null && !dto.getFunctionRelId().trim().isEmpty() && !dto.getFunctionRelId().startsWith("temp-")) {
            Integer frId = Integer.valueOf(dto.getFunctionRelId());
            FunctionalRequirement fr = functionalRequirementRepository.findById(frId)
                    .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", frId));
            usecase.setFunctionalRequirement(fr);
        }

        usecaseRepository.save(usecase);
        log.info("Usecase id={} updated successfully", item.getEntityId());

        updateUsecaseRelatedEntities(usecase, dto);
    }

    private void deleteUsecase(ChangeItem item) {
        log.info("Deleting Usecase id={}", item.getEntityId());
        usecaseRepository.deleteById(item.getEntityId());
        log.info("Usecase id={} deleted successfully", item.getEntityId());
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
                } catch (Exception e) {
                    log.warn("Failed to create normal flow: {}", e.getMessage());
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
                } catch (Exception e) {
                    log.warn("Failed to create alternative flow: {}", e.getMessage());
                }
            }
        }

        // Create business rule links
        if (dto.getLinkedBusinessRuleIds() != null) {
            for (String brIdStr : dto.getLinkedBusinessRuleIds()) {
                if (brIdStr == null || brIdStr.trim().isEmpty() || brIdStr.startsWith("temp-")) {
                    continue; // Skip temporary or invalid links
                }
                try {
                    Integer brId = Integer.valueOf(brIdStr);
                    BusinessRule br = businessRuleRepository.findById(brId)
                            .orElseThrow(() -> new EntityNotFoundException("BusinessRule", brId));
                    UsecaseBusinessRule ubr = new UsecaseBusinessRule(usecase, br);
                    usecaseBusinessRuleRepository.save(ubr);
                } catch (Exception e) {
                    log.warn("Failed to link BusinessRule id={}: {}", brIdStr, e.getMessage());
                }
            }
        }
        
        // Link actor
        if (dto.getActor() != null && !dto.getActor().trim().isEmpty()) {
            String actorName = dto.getActor().trim();
            // Try to find existing actor in this project, or create a new one
            Actor actor = actorRepository.findByProject_ProjectId(usecase.getProject().getProjectId())
                    .stream()
                    .filter(a -> a.getActorName().equalsIgnoreCase(actorName))
                    .findFirst()
                    .orElseGet(() -> {
                        Actor newActor = new Actor();
                        newActor.setProject(usecase.getProject());
                        newActor.setActorName(actorName);
                        return actorRepository.save(newActor);
                    });
            
            try {
                UsecaseActor ua = new UsecaseActor(usecase, actor);
                usecaseActorRepository.save(ua);
            } catch (Exception e) {
                log.warn("Failed to link Actor {}: {}", actorName, e.getMessage());
            }
        }
        
        // Save Diagram URL
        if (dto.getDiagramUrl() != null && !dto.getDiagramUrl().trim().isEmpty()) {
            UsecaseDiagramUrl urlEntity = usecaseDiagramUrlRepository.findById(usecase.getUsecaseId())
                    .orElseGet(() -> {
                        UsecaseDiagramUrl udu = new UsecaseDiagramUrl();
                        udu.setUsecase(usecase);
                        return udu;
                    });
            urlEntity.setDiagramUrl(dto.getDiagramUrl());
            usecaseDiagramUrlRepository.save(urlEntity);
        } else {
            // If it's cleared or null, remove the existing diagram if any
            usecaseDiagramUrlRepository.findById(usecase.getUsecaseId()).ifPresent(usecaseDiagramUrlRepository::delete);
        }
    }

    private void updateUsecaseRelatedEntities(Usecase usecase, UsecasePayloadDto dto) {
        log.info("Updating related entities for Usecase id={}", usecase.getUsecaseId());

        try {
            usecaseFlowRepository.deleteByUsecase_UsecaseId(usecase.getUsecaseId());
            usecaseBusinessRuleRepository.deleteByUsecase_UsecaseId(usecase.getUsecaseId());
        } catch (Exception e) {
            log.warn("Error deleting old relationships for Usecase id={}: {}", usecase.getUsecaseId(), e.getMessage());
        }

        createUsecaseRelatedEntities(usecase, dto);
        log.info("Related entities updated for Usecase id={}", usecase.getUsecaseId());
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
        log.info("Creating VisionScope");
        VisionScopePayloadDto dto = parseJson(item.getNewValue(), VisionScopePayloadDto.class);
        VisionScope vs = visionScopeMapper.toEntity(dto);
        vs.setProject(project);
        visionScopeRepository.save(vs);
        log.info("VisionScope created successfully");
    }

    private void updateVisionScope(ChangeItem item) {
        log.info("Updating VisionScope id={}", item.getEntityId());
        VisionScope vs = visionScopeRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("VisionScope", item.getEntityId()));
        VisionScopePayloadDto dto = parseJson(item.getNewValue(), VisionScopePayloadDto.class);
        visionScopeMapper.updateEntity(dto, vs);
        visionScopeRepository.save(vs);
        log.info("VisionScope id={} updated successfully", item.getEntityId());
    }

    private void deleteVisionScope(ChangeItem item) {
        log.info("Deleting VisionScope id={}", item.getEntityId());
        visionScopeRepository.deleteById(item.getEntityId());
        log.info("VisionScope id={} deleted successfully", item.getEntityId());
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
        log.info("Creating Constraint");
        ConstraintPayloadDto dto = parseJson(item.getNewValue(), ConstraintPayloadDto.class);
        Constraint c = constraintMapper.toEntity(dto);
        c.setProject(project);
        constraintRepository.save(c);
        log.info("Constraint created successfully");
    }

    private void updateConstraint(ChangeItem item) {
        log.info("Updating Constraint id={}", item.getEntityId());
        Constraint c = constraintRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("Constraint", item.getEntityId()));
        ConstraintPayloadDto dto = parseJson(item.getNewValue(), ConstraintPayloadDto.class);
        constraintMapper.updateEntity(dto, c);
        constraintRepository.save(c);
        log.info("Constraint id={} updated successfully", item.getEntityId());
    }

    private void deleteConstraint(ChangeItem item) {
        log.info("Deleting Constraint id={}", item.getEntityId());
        constraintRepository.deleteById(item.getEntityId());
        log.info("Constraint id={} deleted successfully", item.getEntityId());
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
        log.info("Creating FunctionalRequirement");
        FunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), FunctionalRequirementPayloadDto.class);
        FunctionalRequirement req = functionalReqMapper.toEntity(dto);
        req.setProject(project);
        functionalRequirementRepository.save(req);
        log.info("FunctionalRequirement created successfully");
    }

    private void updateFunctionalReq(ChangeItem item) {
        log.info("Updating FunctionalRequirement id={}", item.getEntityId());
        FunctionalRequirement req = functionalRequirementRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("FunctionalRequirement", item.getEntityId()));
        FunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), FunctionalRequirementPayloadDto.class);
        functionalReqMapper.updateEntity(dto, req);
        functionalRequirementRepository.save(req);
        log.info("FunctionalRequirement id={} updated successfully", item.getEntityId());
    }

    private void deleteFunctionalReq(ChangeItem item) {
        log.info("Deleting FunctionalRequirement id={}", item.getEntityId());
        functionalRequirementRepository.deleteById(item.getEntityId());
        log.info("FunctionalRequirement id={} deleted successfully", item.getEntityId());
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
        log.info("Creating NonFunctionalRequirement");
        NonFunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), NonFunctionalRequirementPayloadDto.class);
        NonFunctionalRequirement req = nonFunctionalReqMapper.toEntity(dto);
        req.setProject(project);
        nonFunctionalRequirementRepository.save(req);
        log.info("NonFunctionalRequirement created successfully");
    }

    private void updateNonFunctionalReq(ChangeItem item) {
        log.info("Updating NonFunctionalRequirement id={}", item.getEntityId());
        NonFunctionalRequirement req = nonFunctionalRequirementRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("NonFunctionalRequirement", item.getEntityId()));
        NonFunctionalRequirementPayloadDto dto = parseJson(item.getNewValue(), NonFunctionalRequirementPayloadDto.class);
        nonFunctionalReqMapper.updateEntity(dto, req);
        nonFunctionalRequirementRepository.save(req);
        log.info("NonFunctionalRequirement id={} updated successfully", item.getEntityId());
    }

    private void deleteNonFunctionalReq(ChangeItem item) {
        log.info("Deleting NonFunctionalRequirement id={}", item.getEntityId());
        nonFunctionalRequirementRepository.deleteById(item.getEntityId());
        log.info("NonFunctionalRequirement id={} deleted successfully", item.getEntityId());
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
        log.info("Creating BusinessRule");
        BusinessRulePayloadDto dto = parseJson(item.getNewValue(), BusinessRulePayloadDto.class);
        BusinessRule rule = businessRuleMapper.toEntity(dto);
        rule.setProject(project);
        businessRuleRepository.save(rule);
        log.info("BusinessRule created successfully");
    }

    private void updateBusinessRule(ChangeItem item) {
        log.info("Updating BusinessRule id={}", item.getEntityId());
        BusinessRule rule = businessRuleRepository.findById(item.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException("BusinessRule", item.getEntityId()));
        BusinessRulePayloadDto dto = parseJson(item.getNewValue(), BusinessRulePayloadDto.class);
        businessRuleMapper.updateEntity(dto, rule);
        businessRuleRepository.save(rule);
        log.info("BusinessRule id={} updated successfully", item.getEntityId());
    }

    private void deleteBusinessRule(ChangeItem item) {
        log.info("Deleting BusinessRule id={}", item.getEntityId());
        businessRuleRepository.deleteById(item.getEntityId());
        log.info("BusinessRule id={} deleted successfully", item.getEntityId());
    }

    // ---- ACTOR ----
    private void applyActorChange(ChangeItem item, Project project) {
        switch (item.getOperation()) {
            case "CREATE" -> {
                Actor actor = new Actor();
                actor.setProject(project);
                actor.setActorName(item.getFieldName());
                actorRepository.save(actor);
                log.info("Actor created successfully");
            }
            case "UPDATE" -> {
                Actor actor = actorRepository.findById(item.getEntityId())
                        .orElseThrow(() -> new EntityNotFoundException("Actor", item.getEntityId()));
                actor.setActorName(item.getFieldName());
                actorRepository.save(actor);
                log.info("Actor id={} updated successfully", item.getEntityId());
            }
            case "DELETE" -> {
                actorRepository.deleteById(item.getEntityId());
                log.info("Actor id={} deleted successfully", item.getEntityId());
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
                log.info("Updating Project id={}", item.getEntityId());
                projectRepository.save(project);
                log.info("Project id={} updated successfully", item.getEntityId());
            }
            case "DELETE" -> {
                projectRepository.deleteById(item.getEntityId());
                log.info("Project id={} deleted successfully", item.getEntityId());
            }
            case "CREATE" -> {
                log.warn("CREATE PROJECT via ChangeRequest is not allowed");
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
