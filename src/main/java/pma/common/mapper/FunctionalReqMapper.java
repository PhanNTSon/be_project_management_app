package pma.common.mapper;

import org.mapstruct.Mapper;
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

    // Payload DTOs
    FunctionalRequirement toEntity(FunctionalRequirementPayloadDto dto);
    FunctionalRequirementPayloadDto toPayloadDto(FunctionalRequirement functionalRequirement);
    void updateEntity(FunctionalRequirementPayloadDto dto, @MappingTarget FunctionalRequirement entity);
}
