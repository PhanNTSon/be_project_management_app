package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pma.project.dto.response.ResponseChangeRequestDto;
import pma.project.dto.response.ResponseChangeRequestDto.ResponseChangeItemDto;
import pma.project.entity.change.ChangeItem;
import pma.project.entity.change.ChangeRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChangeRequestMapper {

    @Mapping(source = "requester.fullName", target = "requesterName")
    @Mapping(target = "items", ignore = true)
    ResponseChangeRequestDto toDto(ChangeRequest changeRequest);

    ResponseChangeItemDto itemToDto(ChangeItem changeItem);

    List<ResponseChangeRequestDto> toDtoList(List<ChangeRequest> requests);
}
