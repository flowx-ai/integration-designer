package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.IntegrationSystem;
import ai.flowx.integration.domain.Variable;
import ai.flowx.integration.dto.IntegrationSystemDTO;
import ai.flowx.integration.dto.IntegrationSystemSummaryDTO;
import ai.flowx.integration.dto.VariableDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IntegrationSystemMapper {

    VariableMapper VARIABLE_MAPPER = Mappers.getMapper(VariableMapper.class);

    @Mapping(source = "variables", target = "variables", qualifiedByName = "toVariableDtoList")
    IntegrationSystemDTO toDto(IntegrationSystem entity);

    IntegrationSystemSummaryDTO toSummaryDto(IntegrationSystem entity);

    @Mapping(source = "variables", target = "variables", qualifiedByName = "toVariableEntityList")
    IntegrationSystem toEntity(IntegrationSystemDTO dto);

    IntegrationSystem toEntity(IntegrationSystemSummaryDTO dto);

    @Named("toVariableDtoList")
    default List<VariableDTO> toVariableDtoList(List<Variable> variableList) {
        if (CollectionUtils.isEmpty(variableList)) {
            return new ArrayList<>();
        }
        return VARIABLE_MAPPER.toDto(variableList);
    }

    @Named("toVariableEntityList")
    default List<Variable> toVariableEntityList(List<VariableDTO> variableList) {
        if (CollectionUtils.isEmpty(variableList)) {
            return new ArrayList<>();
        }
        return VARIABLE_MAPPER.toEntity(variableList);
    }
}
