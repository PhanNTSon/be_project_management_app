package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.FunctionalRequirementPayloadDto;
import pma.project.dto.response.ResponseFunctionalReqDto;
import pma.project.entity.core.FunctionalRequirement;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FunctionalReqMapper {

    // Response DTOs
    ResponseFunctionalReqDto toDto(FunctionalRequirement functionalRequirement);
    List<ResponseFunctionalReqDto> toDtoList(List<FunctionalRequirement> requirements);

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "requirementId", ignore = true)
    @Mapping(target = "project", ignore = true)
    FunctionalRequirement toEntity(FunctionalRequirementPayloadDto dto);

    FunctionalRequirementPayloadDto toPayloadDto(FunctionalRequirement functionalRequirement);

    @Mapping(target = "requirementId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(FunctionalRequirementPayloadDto dto, @MappingTarget FunctionalRequirement entity);
}
