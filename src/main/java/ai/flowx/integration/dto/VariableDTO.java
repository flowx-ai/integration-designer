package ai.flowx.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariableDTO {
    private String id;
    private String key;
    private String type;
    private String value;
    private List<String> environments;
}
