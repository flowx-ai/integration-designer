package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.Authorization;
import ai.flowx.integration.domain.IntegrationSystem;
import ai.flowx.integration.domain.Variable;
import ai.flowx.integration.dto.AuthorizationDTO;
import ai.flowx.integration.dto.IntegrationSystemDTO;
import ai.flowx.integration.dto.IntegrationSystemSummaryDTO;
import ai.flowx.integration.dto.VariableDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.mapper.AuthorizationMapper;
import ai.flowx.integration.mapper.IntegrationSystemMapper;
import ai.flowx.integration.mapper.VariableMapper;
import ai.flowx.integration.repository.IntegrationSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static ai.flowx.integration.exceptions.ExceptionMessages.*;

@RequiredArgsConstructor
@Component
public class IntegrationSystemService {
    private final IntegrationSystemRepository integrationSystemRepository;
    private final IntegrationSystemMapper integrationSystemMapper;
    private final AuthorizationMapper authorizationMapper;
    private final VariableMapper variableMapper;
    private final EndpointService endpointService;

    public List<IntegrationSystemSummaryDTO> getAllSystemSummaries() {
        return integrationSystemRepository.findAllSummaries().stream()
                .map(integrationSystemMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public Optional<IntegrationSystemDTO> findOneById(String systemId) {
        return integrationSystemRepository.findById(systemId)
                .map(integrationSystemMapper::toDto)
                .map(dto -> {
                    dto.setEndpoints(endpointService.getAllEndpointsSummariesBySystemId(dto.getId()));
                    return dto;
                });
    }

    public IntegrationSystemSummaryDTO save(IntegrationSystemSummaryDTO systemSummaryDTO) {
        validateCode(systemSummaryDTO);

        IntegrationSystem entity = integrationSystemMapper.toEntity(systemSummaryDTO);
        entity.setFlowxUuid(UUID.randomUUID().toString());
        return integrationSystemMapper.toSummaryDto(integrationSystemRepository.save(entity));
    }

    public void deleteSystem(String systemId) {
        integrationSystemRepository.deleteById(systemId);
    }

    public VariableDTO saveVariable(String systemId, VariableDTO variableDTO) {
        IntegrationSystem existingIntegration = integrationSystemRepository.findVariablesBySystemId(systemId)
                .orElseThrow(() -> new BadRequestAlertException(SYSTEM_NOT_FOUND, IntegrationSystem.class.getName(), BadRequestErrorType.SYSTEM_NOT_FOUND));

        validateVariable(existingIntegration, variableDTO);
        Variable entity = variableMapper.toEntity(variableDTO);
        return variableMapper.toDto(integrationSystemRepository.saveVariable(systemId, entity));
    }

    public VariableDTO updateVariable(String systemId, VariableDTO variableDTO) {
        IntegrationSystem existingIntegration = integrationSystemRepository.findVariablesBySystemId(systemId)
                .orElseThrow(() -> new BadRequestAlertException(SYSTEM_NOT_FOUND, IntegrationSystem.class.getName(), BadRequestErrorType.SYSTEM_NOT_FOUND));

        Variable existingVariable = Optional.ofNullable(existingIntegration.getVariables()).flatMap(var -> var.stream().filter(v -> Objects.equals(v.getId(), variableDTO.getId())).findFirst())
                .orElseThrow(() -> new BadRequestAlertException(VARIABLE_NOT_FOUND, IntegrationSystem.class.getName(), BadRequestErrorType.VARIABLE_NOT_FOUND));
        if (!Objects.equals(existingVariable.getKey(), variableDTO.getKey())) {
            validateVariable(existingIntegration, variableDTO);
        }
        Variable entity = variableMapper.toEntity(variableDTO);
        return variableMapper.toDto(integrationSystemRepository.updateVariable(systemId, entity));
    }

    public void deleteVariable(String systemId, String variableId) {
        IntegrationSystem existingSystem = findSummaryMandatory(systemId);
        integrationSystemRepository.deleteVariable(systemId, variableId);
    }

    public IntegrationSystemSummaryDTO updateGeneral(String id, IntegrationSystemSummaryDTO systemSummaryDTO) {
        IntegrationSystem existingSystem = findSummaryMandatory(id);

        if (!Objects.equals(existingSystem.getCode(), systemSummaryDTO.getCode())) {
            validateCode(systemSummaryDTO);
        }
        IntegrationSystem entity = integrationSystemMapper.toEntity(systemSummaryDTO);
        entity.setId(id);
        integrationSystemRepository.updateGeneral(entity);
        return integrationSystemMapper.toSummaryDto(findSummaryMandatory(id));
    }

    public AuthorizationDTO updateAuthorization(String id, AuthorizationDTO authorizationDTO) {
        IntegrationSystem existingSystem = findSummaryMandatory(id);
        validateAuthorization(authorizationDTO);
        Authorization entity = authorizationMapper.toEntity(authorizationDTO);
        entity = integrationSystemRepository.updateAuthorization(id, entity);
        return authorizationMapper.toDto(entity);
    }

    private void validateCode(IntegrationSystemSummaryDTO systemSummaryDTO) {
        if (integrationSystemRepository.existsByCode(systemSummaryDTO.getCode())) {
            throw new BadRequestAlertException(SYSTEM_CODE_EXISTS, IntegrationSystem.class.getName(), BadRequestErrorType.CODE_EXISTS);
        }
    }

    private IntegrationSystem findSummaryMandatory(String id) {
        return integrationSystemRepository.findSummaryById(id)
                .orElseThrow(() -> new BadRequestAlertException(SYSTEM_NOT_FOUND, IntegrationSystem.class.getName(), BadRequestErrorType.SYSTEM_NOT_FOUND));
    }

    private void validateAuthorization(AuthorizationDTO authorizationDTO) {
        //add here validations based on type when everything is getting more clear
    }

    private void validateVariable(IntegrationSystem integrationSystem, VariableDTO variableDTO) {
        Set<String> keys = Optional.ofNullable(integrationSystem.getVariables())
                .map(v -> v.stream().map(Variable::getKey).collect(Collectors.toSet()))
                .orElseGet(HashSet::new);

        if (keys.contains(variableDTO.getKey())) {
            throw new BadRequestAlertException(SYSTEM_VARIABLE_KEY_EXISTS, IntegrationSystem.class.getName(), BadRequestErrorType.VARIABLE_KEY_EXISTS);
        }
    }
}
