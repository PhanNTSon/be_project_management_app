package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseConstraintDto;
import pma.project.entity.core.Constraint;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConstraintMapper {
    ResponseConstraintDto toDto(Constraint constraint);
    List<ResponseConstraintDto> toDtoList(List<Constraint> constraints);
}
