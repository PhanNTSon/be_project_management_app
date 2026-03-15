package pma.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    // Payload DTOs: ignore DB-managed and relationship fields — set by service
    @Mapping(target = "ruleId", ignore = true)
    @Mapping(target = "project", ignore = true)
    BusinessRule toEntity(BusinessRulePayloadDto dto);

    BusinessRulePayloadDto toPayloadDto(BusinessRule businessRule);

    @Mapping(target = "ruleId", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(BusinessRulePayloadDto dto, @MappingTarget BusinessRule entity);
}
