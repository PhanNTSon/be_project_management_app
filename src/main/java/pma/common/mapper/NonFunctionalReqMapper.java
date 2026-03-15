package pma.common.mapper;

import org.mapstruct.Mapper;
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

    // Payload DTOs
    NonFunctionalRequirement toEntity(NonFunctionalRequirementPayloadDto dto);
    NonFunctionalRequirementPayloadDto toPayloadDto(NonFunctionalRequirement nonFunctionalRequirement);
    void updateEntity(NonFunctionalRequirementPayloadDto dto, @MappingTarget NonFunctionalRequirement entity);
}
