package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.NonFunctionalRequirementPayloadDto;
import pma.project.dto.response.ResponseNonFunctionalReqDto;
import pma.project.entity.core.NonFunctionalRequirement;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NonFunctionalReqMapper {

    // Response DTOs
    ResponseNonFunctionalReqDto toDto(NonFunctionalRequirement nonFunctionalRequirement);
    List<ResponseNonFunctionalReqDto> toDtoList(List<NonFunctionalRequirement> requirements);

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "requirementId", ignore = true)
    @Mapping(target = "project", ignore = true)
    NonFunctionalRequirement toEntity(NonFunctionalRequirementPayloadDto dto);

    NonFunctionalRequirementPayloadDto toPayloadDto(NonFunctionalRequirement nonFunctionalRequirement);

    @Mapping(target = "requirementId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(NonFunctionalRequirementPayloadDto dto, @MappingTarget NonFunctionalRequirement entity);
}
