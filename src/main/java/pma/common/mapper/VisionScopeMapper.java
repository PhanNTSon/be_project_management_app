package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseVisionScopeDto;
import pma.project.entity.core.VisionScope;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VisionScopeMapper {
    ResponseVisionScopeDto toDto(VisionScope visionScope);
    List<ResponseVisionScopeDto> toDtoList(List<VisionScope> visionScopes);
}
