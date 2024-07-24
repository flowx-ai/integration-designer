package ai.flowx.integration.web.rest;

import ai.flowx.integration.dto.*;
import ai.flowx.integration.dto.enums.ParamType;
import ai.flowx.integration.service.EndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class EndpointResource {
    private final EndpointService endpointService;

    @PostMapping("/{systemId}/endpoints")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointSummaryDTO> addEndpoint(@PathVariable String systemId, @Valid @RequestBody EndpointSummaryDTO endpointDTO) {
        log.debug("REST request to add new endpoint: {} for system: {}", endpointDTO, systemId);

        return ResponseEntity.ok().body(endpointService.createEndpoint(systemId, endpointDTO));
    }

    @PutMapping("/{systemId}/endpoints/general")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointSummaryDTO> updateEndpoint(@PathVariable String systemId, @Valid @RequestBody EndpointSummaryDTO endpointDTO) {
        log.debug("REST request to update endpoint: {} system: {}", endpointDTO, systemId);

        return ResponseEntity.ok().body(endpointService.updateEndpoint(systemId, endpointDTO));
    }

    @PatchMapping("/{systemId}/endpoints/general")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<SystemEndpointSummaryDTO> updateSummaryEndpoint(@PathVariable String systemId, @Valid @RequestBody SystemEndpointSummaryDTO endpointDTO) {
        log.debug("REST request to patch endpoint: {} system: {}", endpointDTO, systemId);

        return ResponseEntity.ok().body(endpointService.updateSummary(systemId, endpointDTO));
    }

    @DeleteMapping("/{systemId}/endpoints/{endpointId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable String systemId, @PathVariable String endpointId) {
        log.debug("REST request to delete endpoint: {} system: {}", systemId, endpointId);

        endpointService.deleteEndpoint(endpointId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{systemId}/endpoints/{endpointId}/params")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointParamDTO> addParam(@PathVariable String systemId, @PathVariable String endpointId, @Valid @RequestBody GenericEndpointParamDTO paramDTO) {
        log.debug("REST request to add new param: {} to endpoint: {}", paramDTO, endpointId);

        return ResponseEntity.ok().body(endpointService.addParam(endpointId, paramDTO));
    }

    @PutMapping("/{systemId}/endpoints/{endpointId}/params")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointParamDTO> updateParam(@PathVariable String systemId, @PathVariable String endpointId, @Valid @RequestBody GenericEndpointParamDTO paramDTO) {
        log.debug("REST request to update param: {} endpoint: {}", paramDTO, endpointId);

        return ResponseEntity.ok().body(endpointService.updateParam(endpointId, paramDTO));
    }

    @DeleteMapping("/{systemId}/endpoints/{endpointId}/params/{paramId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteParam(@PathVariable String systemId, @PathVariable String endpointId, @PathVariable String paramId,
                                            @RequestParam ParamType type) {
        log.debug("REST request to delete param: {} endpoint: {}", paramId, endpointId);

        endpointService.deleteParam(endpointId, paramId, type);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{systemId}/endpoints/{endpointId}/responses")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointResponseDTO> addResponse(@PathVariable String systemId, @PathVariable String endpointId,
                                                           @Valid @RequestBody EndpointResponseDTO endpointResponseDTO) {
        log.debug("REST request to add new response: {} to endpoint: {}", endpointResponseDTO, endpointId);

        return ResponseEntity.ok().body(endpointService.addResponse(endpointId, endpointResponseDTO));
    }

    @PutMapping("/{systemId}/endpoints/{endpointId}/responses")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<EndpointResponseDTO> updateResponse(@PathVariable String systemId, @PathVariable String endpointId,
                                                              @Valid @RequestBody EndpointResponseDTO endpointResponseDTO) {
        log.debug("REST request to update response: {} endpoint: {}", endpointResponseDTO, endpointId);

        return ResponseEntity.ok().body(endpointService.updateResponse(endpointId, endpointResponseDTO));
    }

    @DeleteMapping("/{systemId}/endpoints/{endpointId}/responses/{responseId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteResponse(@PathVariable String systemId, @PathVariable String endpointId, @PathVariable String responseId) {
        log.debug("REST request to delete response: {} endpoint: {}", responseId, endpointId);

        endpointService.deleteResponse(endpointId, responseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/endpoints/{endpointId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'read')))")
    public ResponseEntity<EndpointWithSystemSummaryDTO> getEndpoint(@PathVariable String endpointId) {
        log.debug("REST request to get Endpoint : {}", endpointId);
        return ResponseEntity.ok().body(endpointService.getEndpointWithSystemMandatory(endpointId));

    }
}
