package ai.flowx.integration.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EndpointWithSystem extends Endpoint{
    private IntegrationSystem system;
    private Map<String, String> variablesMap = new HashMap<>();
}
