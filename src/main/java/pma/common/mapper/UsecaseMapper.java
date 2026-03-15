package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.UsecasePayloadDto;
import pma.project.dto.response.ResponseUsecaseDto;
import pma.project.entity.usecase.Usecase;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsecaseMapper {
    // Response DTOs
    ResponseUsecaseDto toDto(Usecase usecase);
    List<ResponseUsecaseDto> toDtoList(List<Usecase> usecases);

    // Payload DTOs
    Usecase toEntity(UsecasePayloadDto dto);
    UsecasePayloadDto toPayloadDto(Usecase usecase);
    void updateEntity(UsecasePayloadDto dto, @MappingTarget Usecase entity);
}
