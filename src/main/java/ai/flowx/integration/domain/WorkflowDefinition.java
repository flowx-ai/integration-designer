package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDefinition extends Workflow{
    private List<WorkflowNode> nodes;
}
