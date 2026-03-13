package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseUsecaseDto;
import pma.project.entity.usecase.Usecase;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsecaseMapper {
    ResponseUsecaseDto toDto(Usecase usecase);
    List<ResponseUsecaseDto> toDtoList(List<Usecase> usecases);
}
