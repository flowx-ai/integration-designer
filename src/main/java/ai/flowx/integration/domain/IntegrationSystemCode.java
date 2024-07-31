package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IntegrationSystemCode {
    private String id;
    private String flowxUuid;
    private String code;
}
