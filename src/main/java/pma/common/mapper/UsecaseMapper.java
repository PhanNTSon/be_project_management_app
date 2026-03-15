package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.UsecasePayloadDto;
import pma.project.dto.response.ResponseUsecaseDto;
import pma.project.entity.usecase.Usecase;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsecaseMapper {

    // Response DTOs — relationship fields (actor, flows, BRs, functionRelId) are
    // populated manually in ProjectService after querying child tables.
    @Mapping(target = "actor", ignore = true)
    @Mapping(target = "functionRelId", ignore = true)
    @Mapping(target = "normalFlows", ignore = true)
    @Mapping(target = "alterFlows", ignore = true)
    @Mapping(target = "linkedBusinessRuleIds", ignore = true)
    @Mapping(target = "diagramUrl", ignore = true)
    ResponseUsecaseDto toDto(Usecase usecase);

    List<ResponseUsecaseDto> toDtoList(List<Usecase> usecases);

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "usecaseId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "functionalRequirement", ignore = true)
    Usecase toEntity(UsecasePayloadDto dto);

    @Mapping(target = "functionRelId", ignore = true)
    @Mapping(target = "normalFlows", ignore = true)
    @Mapping(target = "alterFlows", ignore = true)
    @Mapping(target = "linkedBusinessRuleIds", ignore = true)
    @Mapping(target = "actor", ignore = true)
    @Mapping(target = "diagramUrl", ignore = true)
    UsecasePayloadDto toPayloadDto(Usecase usecase);

    @Mapping(target = "usecaseId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "functionalRequirement", ignore = true)
    void updateEntity(UsecasePayloadDto dto, @MappingTarget Usecase entity);
}
