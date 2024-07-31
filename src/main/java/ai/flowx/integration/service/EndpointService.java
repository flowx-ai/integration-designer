package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.*;
import ai.flowx.integration.dto.enums.ParamType;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.mapper.EndpointMapper;
import ai.flowx.integration.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ai.flowx.integration.exceptions.ExceptionMessages.*;

@Component
@RequiredArgsConstructor
public class EndpointService {
    private final EndpointRepository endpointRepository;
    private final EndpointMapper endpointMapper;

    public List<SystemEndpointSummaryDTO> getAllEndpointsSummariesBySystemId(String systemId) {
        return endpointRepository.findAllSummariesBySystemId(systemId).stream()
                .map(endpointMapper::toSystemEndpointSummaryDto)
                .collect(Collectors.toList());
    }

    public EndpointSummaryDTO createEndpoint(String systemId, EndpointSummaryDTO endpointSummaryDTO) {
        Endpoint entity = endpointMapper.toEntity(endpointSummaryDTO);
        entity.setSystemId(systemId);
        entity.setFlowxUuid(UUID.randomUUID().toString());
        return endpointMapper.toSummaryDto(endpointRepository.save(entity));
    }

    public EndpointSummaryDTO updateEndpoint(String systemId, EndpointSummaryDTO endpointSummaryDTO) {
        validateEndpointExists(endpointSummaryDTO.getId());
        Endpoint entity = endpointMapper.toEntity(endpointSummaryDTO);
        return endpointMapper.toSummaryDto(endpointRepository.updateGeneral(entity));
    }

    public SystemEndpointSummaryDTO updateSummary(String systemId, SystemEndpointSummaryDTO endpointSummaryDTO) {
        validateEndpointExists(endpointSummaryDTO.getId());
        return endpointRepository.updateNameMethodAndDescription(endpointSummaryDTO);
    }

    public void deleteEndpoint(String endpointId) {
        endpointRepository.deleteById(endpointId);
    }

    public EndpointParamDTO addParam(String endpointId, GenericEndpointParamDTO endpointParamDTO) {
        validateEndpointExists(endpointId);
        if (endpointParamDTO.getId() != null) {
            throw new BadRequestAlertException(ID_NOT_NULL, Endpoint.class.getName(), BadRequestErrorType.ID_NOT_NULL);
        }

        EndpointParam entity = endpointMapper.toEntity(endpointParamDTO);
        return endpointMapper.toDto(endpointRepository.saveParam(endpointId, endpointParamDTO.getParamType(), entity));
    }

    public EndpointParamDTO updateParam(String endpointId, GenericEndpointParamDTO endpointParamDTO) {
        validateParamExists(endpointId, endpointParamDTO.getId(), endpointParamDTO.getParamType());
        EndpointParam entity = endpointMapper.toEntity(endpointParamDTO);
        return endpointMapper.toDto(endpointRepository.updateParam(endpointId, endpointParamDTO.getParamType(), entity));
    }

    public void deleteParam(String endpointId, String paramId, ParamType type) {
        endpointRepository.deleteParam(endpointId, type, paramId);
    }

    public EndpointResponseDTO addResponse(String endpointId, EndpointResponseDTO endpointResponse) {
        validateEndpointExists(endpointId);
        EndpointResponse entity = endpointMapper.toEntity(endpointResponse);
        return endpointMapper.toDto(endpointRepository.saveResponse(endpointId, entity));
    }

    public EndpointResponseDTO updateResponse(String endpointId, EndpointResponseDTO endpointResponse) {
        validateEndpointResponseExists(endpointId, endpointResponse.getId());
        EndpointResponse entity = endpointMapper.toEntity(endpointResponse);
        return endpointMapper.toDto(endpointRepository.updateResponse(endpointId, entity));
    }

    public void deleteResponse(String endpointId, String responseId) {
        endpointRepository.deleteResponse(endpointId, responseId);
    }

    public EndpointWithSystemSummaryDTO getEndpointWithSystemMandatory(String endpointId) {
        EndpointWithSystemSummaryDTO endpoint = endpointRepository.getEndpointWithSystem(endpointId)
                .map(endpointMapper::toDto)
                .orElseThrow(() -> new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND));
        if (endpoint.getSystem() == null) {
            throw new BadRequestAlertException(SYSTEM_NOT_FOUND, IntegrationSystem.class.getName(), BadRequestErrorType.SYSTEM_NOT_FOUND);
        }
        return endpoint;
    }

    public EndpointWithSystem getEndpointWithSystem(String endpointId) {
        return endpointRepository.getEndpointWithSystem(endpointId)
                .orElseThrow(() -> new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND));
    }

    public EndpointMetadata getEndpointMetadata(String endpointFlowxUuid){
        return endpointRepository.getEndpointMetadata(endpointFlowxUuid)
                .orElseThrow(() -> new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND));
    }

    private void validateParamExists(String endpointId, String paramId, ParamType type) {
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND));

        Optional<List<EndpointParam>> paramsOpt = ParamType.HEADER.equals(type) ? Optional.ofNullable(endpoint.getHeaders()) : Optional.ofNullable(endpoint.getQueryParameters());
        paramsOpt.flatMap(list -> list.stream().filter(ep -> Objects.equals(ep.getId(), paramId)).findFirst())
                .orElseThrow(() -> new BadRequestAlertException(PARAMETER_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.PARAMETER_NOT_FOUND));
    }

    private void validateEndpointResponseExists(String endpointId, String responseId) {
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND));

        Optional.ofNullable(endpoint.getResponses())
                .flatMap(list -> list.stream().filter(resp -> Objects.equals(resp.getId(), responseId)).findFirst())
                .orElseThrow(() -> new BadRequestAlertException(RESPONSE_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.RESPONSE_NOT_FOUND));
    }

    private void validateEndpointExists(String endpointId) {
        if (!endpointRepository.existsById(endpointId)) {
            throw new BadRequestAlertException(ENDPOINT_NOT_FOUND, Endpoint.class.getName(), BadRequestErrorType.ENDPOINT_NOT_FOUND);
        }
    }
}
