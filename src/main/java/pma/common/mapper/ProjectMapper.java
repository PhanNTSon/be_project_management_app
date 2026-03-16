package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseProjectListDto;
import pma.project.entity.core.Project;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ResponseProjectListDto toDto(Project project);
    List<ResponseProjectListDto> toDtoList(List<Project> projects);
}
