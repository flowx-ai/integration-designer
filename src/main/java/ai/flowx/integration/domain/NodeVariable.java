package ai.flowx.integration.domain;

import ai.flowx.integration.domain.enums.NodeVariableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeVariable {
    private String id;
    private String key;
    private NodeVariableType type;
    private String value;
}