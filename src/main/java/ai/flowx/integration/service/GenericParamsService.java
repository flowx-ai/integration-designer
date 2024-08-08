package ai.flowx.integration.service;

import ai.flowx.integration.domain.Variable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenericParamsService {

    public Map<String, Object> getSystemVariablesValues(List<Variable> systemVariables) {
        //TODO implement generic params
        return new HashMap<>();
    }
}
