package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.project.dto.response.ResponseBusinessRuleDto;
import pma.project.entity.usecase.BusinessRule;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessRuleMapper {
    ResponseBusinessRuleDto toDto(BusinessRule businessRule);
    List<ResponseBusinessRuleDto> toDtoList(List<BusinessRule> businessRules);
}
