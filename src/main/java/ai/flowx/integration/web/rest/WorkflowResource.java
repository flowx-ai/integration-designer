package ai.flowx.integration.web.rest;

import ai.flowx.integration.dto.*;
import ai.flowx.integration.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowResource {
    private final WorkflowService workflowService;

    @GetMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'read')))")
    public ResponseEntity<List<WorkflowDTO>> getAllWorkflows() {
        log.debug("REST request to get all Workflows.");
        return ResponseEntity.ok().body(workflowService.getAllWorkflows());
    }

    @GetMapping("/{workflowId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'read')))")
    public ResponseEntity<WorkflowDefinitionDTO> getWorkflow(@PathVariable String workflowId) {
        log.debug("REST request to get Workflow : {}", workflowId);
        return workflowService.findOneById(workflowId)
                .map(dto -> ResponseEntity.ok().body(dto))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<WorkflowDTO> createWorkflow(@Valid @RequestBody WorkflowDTO workflowDTO) {
        log.debug("REST request to create new workflow: {}", workflowDTO);

        return ResponseEntity.ok().body(workflowService.createWorkflow(workflowDTO));
    }

    @PutMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<WorkflowDTO> updateWorkflow(@Valid @RequestBody WorkflowDTO workflowDTO) {
        log.debug("REST request to update workflow: {}", workflowDTO);

        return ResponseEntity.ok().body(workflowService.updateWorkflow(workflowDTO));
    }

    @DeleteMapping("/{workflowId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Void> deleteSystem(@PathVariable String workflowId) {
        log.debug("REST request to delete workflow: {}", workflowId);

        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.noContent().build();
    }

}
