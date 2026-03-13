package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseNonFunctionalReqDto;
import pma.project.entity.core.NonFunctionalRequirement;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NonFunctionalReqMapper {
    ResponseNonFunctionalReqDto toDto(NonFunctionalRequirement nonFunctionalRequirement);
    List<ResponseNonFunctionalReqDto> toDtoList(List<NonFunctionalRequirement> requirements);
}
