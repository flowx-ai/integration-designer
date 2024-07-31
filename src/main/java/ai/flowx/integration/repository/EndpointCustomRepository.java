package ai.flowx.integration.repository;

import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.SystemEndpointSummaryDTO;
import ai.flowx.integration.dto.enums.ParamType;

import java.util.Optional;


public interface EndpointCustomRepository {
    Endpoint updateGeneral(Endpoint entity);

    EndpointParam saveParam(String endpointId, ParamType type, EndpointParam endpointParam);

    EndpointParam updateParam(String endpointId, ParamType type, EndpointParam endpointParam);

    void deleteParam(String endpointId, ParamType type, String paramId);

    EndpointResponse saveResponse(String endpointId, EndpointResponse endpointResponse);

    EndpointResponse updateResponse(String endpointId, EndpointResponse endpointResponse);

    void deleteResponse(String endpointId, String endpointResponseId);

    SystemEndpointSummaryDTO updateNameMethodAndDescription(SystemEndpointSummaryDTO endpointSummaryDTO);

    Optional<EndpointWithSystem> getEndpointWithSystem(String endpointId);

    Optional<EndpointMetadata> getEndpointMetadata(String endpointFlowxUuid);
}
