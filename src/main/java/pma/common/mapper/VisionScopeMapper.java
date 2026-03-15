package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.VisionScopePayloadDto;
import pma.project.dto.response.ResponseVisionScopeDto;
import pma.project.entity.core.VisionScope;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VisionScopeMapper {

    // Response DTOs
    ResponseVisionScopeDto toDto(VisionScope visionScope);
    List<ResponseVisionScopeDto> toDtoList(List<VisionScope> visionScopes);

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "visionScopeId", ignore = true)
    @Mapping(target = "project", ignore = true)
    VisionScope toEntity(VisionScopePayloadDto dto);

    VisionScopePayloadDto toPayloadDto(VisionScope visionScope);

    @Mapping(target = "visionScopeId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(VisionScopePayloadDto dto, @MappingTarget VisionScope entity);
}
