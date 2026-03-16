package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "constraintId", ignore = true)
    @Mapping(target = "project", ignore = true)
    Constraint toEntity(ConstraintPayloadDto dto);

    ConstraintPayloadDto toPayloadDto(Constraint constraint);

    @Mapping(target = "constraintId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(ConstraintPayloadDto dto, @MappingTarget Constraint entity);
}
