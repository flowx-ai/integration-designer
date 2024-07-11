package ai.flowx.integration.web.rest;

import ai.flowx.integration.dto.AuthorizationDTO;
import ai.flowx.integration.dto.IntegrationSystemDTO;
import ai.flowx.integration.dto.IntegrationSystemSummaryDTO;
import ai.flowx.integration.dto.VariableDTO;
import ai.flowx.integration.service.IntegrationSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemResource {
    private final IntegrationSystemService integrationSystemService;

    @GetMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'read')))")
    public ResponseEntity<List<IntegrationSystemSummaryDTO>> getAllSystems() {
        log.debug("REST request to get all IntegrationSystems.");
        return ResponseEntity.ok().body(integrationSystemService.getAllSystemSummaries());
    }

    @GetMapping("/{systemId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'read')))")
    public ResponseEntity<IntegrationSystemDTO> getIntegrationSystem(@PathVariable String systemId) {
        log.debug("REST request to get IntegrationSystem : {}", systemId);
        return integrationSystemService.findOneById(systemId)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<IntegrationSystemSummaryDTO> createIntegrationSystem(@Valid @RequestBody IntegrationSystemSummaryDTO integrationSystem) {
        log.debug("REST request to create new integrationSystem: {}", integrationSystem);

        return ResponseEntity.ok().body(integrationSystemService.save(integrationSystem));
    }

    @PutMapping("/{systemId}/general")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<IntegrationSystemSummaryDTO> updateIntegrationSystemGeneral(@PathVariable String systemId, @Valid @RequestBody IntegrationSystemSummaryDTO integrationSystem) {
        log.debug("REST request to update integrationSystem: {} with general configuration: {}", systemId, integrationSystem);

        return ResponseEntity.ok().body(integrationSystemService.updateGeneral(systemId, integrationSystem));
    }

    @DeleteMapping("/{systemId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteSystem(@PathVariable String systemId) {
        log.debug("REST request to delete system: {}", systemId);

        integrationSystemService.deleteSystem(systemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{systemId}/authorization")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<AuthorizationDTO> updateIntegrationSystemAuthorization(@PathVariable String systemId, @Valid @RequestBody AuthorizationDTO authorizationDTO) {
        log.debug("REST request to update integrationSystem: {} with authorization configuration: {}", systemId, authorizationDTO);

        return ResponseEntity.ok().body(integrationSystemService.updateAuthorization(systemId, authorizationDTO));
    }

    @PostMapping("/{systemId}/variables")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<VariableDTO> addVariable(@PathVariable String systemId, @Valid @RequestBody VariableDTO variableDTO) {
        log.debug("REST request to create new variable: {} for system: {}", variableDTO, systemId);

        return ResponseEntity.ok().body(integrationSystemService.saveVariable(systemId, variableDTO));
    }

    @PutMapping("/{systemId}/variables")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<VariableDTO> updateVariable(@PathVariable String systemId, @Valid @RequestBody VariableDTO variableDTO) {
        log.debug("REST request to update variable: {} for system: {}", variableDTO, systemId);

        return ResponseEntity.ok().body(integrationSystemService.updateVariable(systemId, variableDTO));
    }

    @DeleteMapping("/{systemId}/variables/{variableId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteVariable(@PathVariable String systemId, @PathVariable String variableId) {
        log.debug("REST request to delete variable: {} from system: {}", variableId, systemId);

        integrationSystemService.deleteVariable(systemId, variableId);
        return ResponseEntity.noContent().build();
    }
}
