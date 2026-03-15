package pma.common.mapper;

import org.mapstruct.Mapper;
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

    // Payload DTOs
    VisionScope toEntity(VisionScopePayloadDto dto);
    VisionScopePayloadDto toPayloadDto(VisionScope visionScope);
    void updateEntity(VisionScopePayloadDto dto, @MappingTarget VisionScope entity);
}
