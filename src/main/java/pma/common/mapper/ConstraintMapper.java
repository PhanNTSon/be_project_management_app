package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.ConstraintPayloadDto;
import pma.project.dto.response.ResponseConstraintDto;
import pma.project.entity.core.Constraint;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConstraintMapper {
    // Response DTOs
    ResponseConstraintDto toDto(Constraint constraint);
    List<ResponseConstraintDto> toDtoList(List<Constraint> constraints);

    // Payload DTOs
    Constraint toEntity(ConstraintPayloadDto dto);
    ConstraintPayloadDto toPayloadDto(Constraint constraint);
    void updateEntity(ConstraintPayloadDto dto, @MappingTarget Constraint entity);
}
