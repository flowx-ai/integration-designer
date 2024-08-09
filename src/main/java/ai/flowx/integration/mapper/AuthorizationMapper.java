package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.Authorization;
import ai.flowx.integration.dto.AuthorizationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorizationMapper {
    AuthorizationDTO toDto(Authorization entity);

    Authorization toEntity(AuthorizationDTO dto);
}
