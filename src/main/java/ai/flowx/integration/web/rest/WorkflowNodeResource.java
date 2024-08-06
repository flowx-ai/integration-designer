package ai.flowx.integration.web.rest;

import ai.flowx.integration.dto.*;
import ai.flowx.integration.service.WorkflowNodeService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/workflows/nodes")
@RequiredArgsConstructor
public class WorkflowNodeResource {
    private final WorkflowNodeService workflowNodeService;

    @PostMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<WorkflowNodeDTO> createWorkflowNode(@Valid @RequestBody CreateWorkflowNodeReqDTO createWorkflowNodeReqDTO) {
        log.debug("REST request to create new workflow node: {}", createWorkflowNodeReqDTO);

        return ResponseEntity.ok().body(workflowNodeService.createNode(createWorkflowNodeReqDTO));
    }

    @PutMapping()
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<WorkflowNodeDTO> updateWorkflowNode(@Valid @RequestBody UpdateWorkflowNodeReqDTO updateWorkflowNodeDTO) {
        log.debug("REST request to update workflow node: {}", updateWorkflowNodeDTO);

        return ResponseEntity.ok().body(workflowNodeService.updateWorkflowNode(updateWorkflowNodeDTO));
    }

    @PatchMapping("/bulk")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<Set<WorkflowNodePositionDTO>> bulkUpdateNodeLayoutOptions(@RequestBody Set<WorkflowNodePositionDTO> workflowNodePositions) {
        log.debug("REST request to update layout options for a list of workflow nodes : {}", workflowNodePositions);

        return ResponseEntity.ok().body(workflowNodeService.updateLayoutOptions(workflowNodePositions));
    }

    @PostMapping("/{workflowNodeId}/sequences")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<SequenceDTO> createSequenceOnWorkflowNode(
            @PathVariable("workflowNodeId") String workflowNodeId,
            @Valid @RequestBody SequenceDTO sequenceDTO) {
        log.debug("REST request to create sequence on workflow node {}: {}", workflowNodeId, sequenceDTO);

        return ResponseEntity.ok().body(workflowNodeService.createSequenceOnWorkflowNode(workflowNodeId, sequenceDTO));
    }

    @PostMapping("/{workflowNodeId}/run")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<NodeRunResponseDTO> runWorkflowNodeIndividually(@PathVariable String workflowNodeId, @Valid @RequestBody JsonNode jsonNode) {
        log.debug("REST request to run workflow node {} individually", workflowNodeId);

        return ResponseEntity.ok().body(workflowNodeService.runWorkflowNodeIndividually(workflowNodeId, jsonNode));
    }

    @DeleteMapping("/{workflowNodeId}")
    @PreAuthorize("hasAnyAuthority((@authorityService.rolesAllowedForAccessAndScope('manage-integrations', 'edit')))")
    public ResponseEntity<List<String>> deleteWorkflowNode(@PathVariable("workflowNodeId") String workflowNodeId) {
        log.debug("REST request to delete workflow node {}", workflowNodeId);

        return ResponseEntity.ok().body(workflowNodeService.deleteWorkflowNodeAndReturnDeletedSequences(workflowNodeId));
    }
}
