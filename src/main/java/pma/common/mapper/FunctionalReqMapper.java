package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseFunctionalReqDto;
import pma.project.entity.core.FunctionalRequirement;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FunctionalReqMapper {
    ResponseFunctionalReqDto toDto(FunctionalRequirement functionalRequirement);
    List<ResponseFunctionalReqDto> toDtoList(List<FunctionalRequirement> requirements);
}
