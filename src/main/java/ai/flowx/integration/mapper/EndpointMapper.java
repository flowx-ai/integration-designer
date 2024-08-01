package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;


import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EndpointMapper {

    EndpointDTO toDto(Endpoint entity);

    EndpointSummaryDTO toSummaryDto(Endpoint entity);

    Endpoint toEntity(EndpointDTO dto);

    Endpoint toEntity(EndpointSummaryDTO dto);

    EndpointParamDTO toDto(EndpointParam entity);

    @Mapping(source = "id", target = "id", qualifiedByName = "generateIdIfNull")
    EndpointParam toEntity(GenericEndpointParamDTO dto);

    EndpointResponseDTO toDto(EndpointResponse entity);

    @Mapping(source = "id", target = "id", qualifiedByName = "generateIdIfNull")
    EndpointResponse toEntity(EndpointResponseDTO dto);

    EndpointWithSystemSummaryDTO toDto(EndpointWithSystem entity);

    SystemEndpointSummaryDTO toSystemEndpointSummaryDto(Endpoint entity);

    SystemEndpointSummaryDTO toSystemEndpointSummaryDto(EndpointSummary entity);

    @Named("generateIdIfNull")
    default String generateIdIfNull(String id) {
        if (StringUtils.hasText(id)) {
            return id;
        }
        return UUID.randomUUID().toString();
    }
}
