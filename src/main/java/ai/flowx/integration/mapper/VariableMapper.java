package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.Variable;
import ai.flowx.integration.dto.VariableDTO;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface VariableMapper {
    VariableDTO toDto(Variable entity);

    @Mapping(source = "id", target = "id", qualifiedByName = "generateIdIfNull")
    Variable toEntity(VariableDTO dto);

    List<Variable> toEntity(List<VariableDTO> dtoList);

    List<VariableDTO> toDto(List<Variable> entityList);

    @Named("generateIdIfNull")
    default String generateIdIfNull(String id) {
        if (StringUtils.hasText(id)) {
            return id;
        }
        return UUID.randomUUID().toString();
    }

}
