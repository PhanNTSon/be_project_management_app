package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pma.project.dto.change.BusinessRulePayloadDto;
import pma.project.dto.response.ResponseBusinessRuleDto;
import pma.project.entity.usecase.BusinessRule;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessRuleMapper {
    // Response DTOs
    ResponseBusinessRuleDto toDto(BusinessRule businessRule);
    List<ResponseBusinessRuleDto> toDtoList(List<BusinessRule> businessRules);

    // Payload DTOs
    BusinessRule toEntity(BusinessRulePayloadDto dto);
    BusinessRulePayloadDto toPayloadDto(BusinessRule businessRule);
    void updateEntity(BusinessRulePayloadDto dto, @MappingTarget BusinessRule entity);
}
