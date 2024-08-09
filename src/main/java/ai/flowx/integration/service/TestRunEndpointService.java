package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.config.AuthConfig;
import ai.flowx.integration.domain.*;
import ai.flowx.integration.domain.enums.AuthorizationType;
import ai.flowx.integration.domain.enums.NodeVariableType;
import ai.flowx.integration.dto.TestEndpointResponseDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ai.flowx.integration.exceptions.ExceptionMessages.WORKFLOW_NODE_RUN_PLACEHOLDER_HAS_NO_VALUE;
import static ai.flowx.integration.exceptions.ExceptionMessages.WORKFLOW_NODE_RUN_REQUIRED_VALUE_NOT_PRESENT;

@RequiredArgsConstructor
@Component
public class TestRunEndpointService {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    private static final String EMPTY_STRING = "";
    private final WebClient webClient;
    private final AuthConfig authConfig;
    private final EndpointService endpointService;
    private final AuthorizationTokenService authorizationTokenService;
    private final ObjectMapper objectMapper;
    private final GenericParamsService genericParamsService;
    private Map<AuthorizationType, TriConsumer<WebClient.RequestBodySpec, Authorization, AuthConfig>> authFunctions = new HashMap<>();

    @PostConstruct
    private void initAuthFunctions(){
        authFunctions = Map.of(
                AuthorizationType.BASIC, (requestSpec, authorization, authConfig) -> requestSpec.headers(httpHeaders -> httpHeaders.setBasicAuth(Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("username")).map(Object::toString).orElse(null), Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("password")).map(Object::toString).orElse(null))),
                AuthorizationType.SERVICE_ACCOUNT, (requestSpec, authorization, authConfig) -> requestSpec.headers(httpHeaders -> httpHeaders.setBearerAuth(authorizationTokenService.getServiceAccountToken(authorization, authConfig)))
        );
    }

    @SneakyThrows
    public Mono<TestEndpointResponseDTO> executeRequestForNodeRun(WorkflowNode workflowNode, Map<String, Object> input) {
        EndpointWithSystem endpoint = endpointService.getEndpointWithSystemUsingUuid(workflowNode.getEndpointFlowxUuid());

        Map<String, Object> variablesFromSystem = genericParamsService.getSystemVariablesValues(endpoint.getSystem().getVariables());
        Map<String, Object> finalValues = objectMapper.readerForUpdating(variablesFromSystem).readValue(objectMapper.writeValueAsString(input));

        Map<NodeVariableType, Map<String, Object>> variablesMappedByTypeAndThenByKey = workflowNode.getVariables()
                .stream()
                .filter(variable -> variable.getValue() != null)
                .collect(Collectors.groupingBy(NodeVariable::getType, Collectors.toMap(NodeVariable::getKey, NodeVariable::getValue)));

        URI uri = getUri(endpoint, finalValues, variablesMappedByTypeAndThenByKey.getOrDefault(NodeVariableType.PARAM, Collections.emptyMap()));

        WebClient.RequestBodySpec requestSpec = webClient
                .method(endpoint.getHttpMethod())
                .uri(uri.toString(), uriBuilder -> {
                    if (!CollectionUtils.isEmpty(endpoint.getQueryParameters())) {
                        endpoint.getQueryParameters()
                                .forEach(qp -> {
                                    Object value = variablesMappedByTypeAndThenByKey.getOrDefault(NodeVariableType.QUERY, Collections.emptyMap())
                                            .getOrDefault(qp.getKey(), EMPTY_STRING);

                                    if(qp.isRequired() && value.equals(EMPTY_STRING)) {
                                        throw new BadRequestAlertException(WORKFLOW_NODE_RUN_REQUIRED_VALUE_NOT_PRESENT.formatted(qp.getKey(), NodeVariableType.QUERY),
                                                Map.class.getName(), BadRequestErrorType.WORKFLOW_NODE_RUN_NOT_VALID);
                                    }

                                    uriBuilder.queryParam(qp.getKey(), replacePlaceholder(Objects.toString(value), finalValues));
                                });
                    }
                    return uriBuilder.build();
                });

        requestSpec.contentType(MediaType.APPLICATION_JSON);

        if (!CollectionUtils.isEmpty(endpoint.getHeaders())) {
            requestSpec.headers(httpHeaders -> {
                for (EndpointParam header : endpoint.getHeaders()) {
                    Object value = variablesMappedByTypeAndThenByKey.getOrDefault(NodeVariableType.HEADER, Collections.emptyMap())
                            .getOrDefault(header.getKey(), EMPTY_STRING);

                    if(header.isRequired() && value.equals(EMPTY_STRING)) {
                        throw new BadRequestAlertException(WORKFLOW_NODE_RUN_REQUIRED_VALUE_NOT_PRESENT.formatted(header.getKey(), NodeVariableType.HEADER),
                                Map.class.getName(), BadRequestErrorType.WORKFLOW_NODE_RUN_NOT_VALID);
                    }

                    httpHeaders.set(header.getKey(), replacePlaceholder(Objects.toString(value), finalValues));
                }
            });
        }

        if (StringUtils.hasText(workflowNode.getPayload())) {
            requestSpec.bodyValue(replacePlaceholder(workflowNode.getPayload(), finalValues));
        }
        //set authorization header
        return makeRequest(requestSpec, endpoint);
    }

    @SneakyThrows
    public Mono<TestEndpointResponseDTO> executeRequest(String endpointId) {
        Map<String, Object> values = getVariableForSystemFromGenericParams();
        EndpointWithSystem endpoint = endpointService.getEndpointWithSystem(endpointId);

        URI uri = getUri(endpoint, values, getValues(endpoint.getPathParameters()));

        WebClient.RequestBodySpec requestSpec = webClient
                .method(endpoint.getHttpMethod())
                .uri(uri.toString(), uriBuilder -> {
                    if (!CollectionUtils.isEmpty(endpoint.getQueryParameters())) {
                        endpoint.getQueryParameters()
                                .forEach(qp -> {
                                    uriBuilder.queryParam(qp.getKey(), qp.getDefaultValue());
                                });
                    }
                    return uriBuilder.build();
                });

        requestSpec.contentType(MediaType.APPLICATION_JSON);

        if (!CollectionUtils.isEmpty(endpoint.getHeaders())) {
            requestSpec.headers(httpHeaders -> {
                for (EndpointParam header : endpoint.getHeaders()) {
                    httpHeaders.set(header.getKey(), header.getDefaultValue());
                }
            });
        }
        if (StringUtils.hasText(endpoint.getPayload())) {
            requestSpec.bodyValue(endpoint.getPayload());
        }
        //set authorization header
        return makeRequest(requestSpec, endpoint);
    }

    private static HashMap<String, Object> getVariableForSystemFromGenericParams() {
        return new HashMap<>();
    }

    private Mono<TestEndpointResponseDTO> makeRequest(WebClient.RequestBodySpec requestSpec, EndpointWithSystem endpoint) {
        setAuthorization(requestSpec, endpoint);
        TestEndpointResponseDTO responseDTO = new TestEndpointResponseDTO();

        requestSpec.httpRequest(req -> responseDTO.setCurlCommand(getCurlCommand(req)));
        long startTime = System.currentTimeMillis();
        //return the response
        return requestSpec.exchangeToMono(response -> {
            HttpStatus statusCode = (HttpStatus) response.statusCode();
            return response.bodyToMono(String.class)
                    .defaultIfEmpty(EMPTY_STRING)
                    .map(body -> {
                        long endTime = System.currentTimeMillis();
                        long responseTime = endTime - startTime;
                        long responseSize = response.headers().contentLength().orElse(0);
                        TestEndpointResponseDTO.TestEndpointResponseDTOBuilder builder = TestEndpointResponseDTO.builder()
                                .code(statusCode)
                                .curlCommand(responseDTO.getCurlCommand())
                                .responseSize(responseSize)
                                .responseTime(responseTime);
                        if (!EMPTY_STRING.equals(body)) {
                            builder.responseSize(body.getBytes().length);
                            try {
                                builder.body(objectMapper.readValue(body, Object.class));
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return builder.build();
                    });
        });
    }

    private String getCurlCommand(ClientHttpRequest request){
        StringBuilder curlCommand = new StringBuilder("curl -X ");
        curlCommand.append(request.getMethod().name()).append(" ");
        curlCommand.append(request.getURI()).append(" ");

        request.getHeaders().entrySet().forEach(entry -> {
            if (entry.getKey().equals("Authorization")) {
                curlCommand.append("-H \"").append(entry.getKey()).append(": ").append("**********");
            } else {
                entry.getValue().forEach(value -> {
                    curlCommand.append("-H \"").append(entry.getKey()).append(": ").append(value).append("\" ");
                });
            }
        });

        return curlCommand.toString();
    }

    private URI getUri(EndpointWithSystem endpoint, Map<String, Object> values, Map<String, Object> paramValues) throws
            MalformedURLException, URISyntaxException {
        URL url = new URL(processUrl(endpoint, values, paramValues));
        // Convert URL to URI
        return url.toURI();
    }

    private String processUrl(EndpointWithSystem endpoint, Map<String, Object> values, Map<String, Object> paramValues) {
        Optional<String> rawBaseUrl = Optional.ofNullable(endpoint.getSystem().getBaseUrl());
        if (rawBaseUrl.isPresent()) {
            String systemUrl = replacePlaceholder(rawBaseUrl.get(), values);
            String endpointUrl = replacePlaceholder(replacePlaceholder(endpoint.getUrl(), paramValues), values);
            return concatUrl(systemUrl, endpointUrl);
        }

        return EMPTY_STRING;
    }

    private Map<String, Object> getValues(List<EndpointParam> params) {
        Map<String, Object> values = new HashMap<>();
        if (CollectionUtils.isEmpty(params)) {
            return values;
        }
        params.forEach(p -> values.put(p.getKey(), p.getDefaultValue()));
        return values;
    }

    private String concatUrl(String systemUrl, String endpointUrl) {
        if (StringUtils.isEmpty(systemUrl)) {
            return endpointUrl;
        }

        if (StringUtils.isEmpty(endpointUrl)) {
            return systemUrl;
        }

        if (systemUrl.endsWith("/") && endpointUrl.startsWith("/")) {
            return systemUrl + endpointUrl.substring(1);
        }

        if (!systemUrl.endsWith("/") && !endpointUrl.startsWith("/")) {
            return systemUrl + "/" + endpointUrl;
        }

        return systemUrl + endpointUrl;
    }

    private String replacePlaceholder(String originalString, Map<String, Object> values) {
        if (StringUtils.isEmpty(originalString)) {
            return EMPTY_STRING;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(originalString);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String dynamicVariableName = matcher.group(1);
            String replacement = Objects.toString(PropertyUtils.getProperty(values, dynamicVariableName), null);
            if (replacement != null) {
                // escape for '$' needed in case replacement is another dynamicVariable (ex. for path params for example)
                matcher.appendReplacement(sb, replacement.replace("$", "\\$"));
            } else {
                throw new BadRequestAlertException(WORKFLOW_NODE_RUN_PLACEHOLDER_HAS_NO_VALUE.formatted(dynamicVariableName), Map.class.getName(), BadRequestErrorType.WORKFLOW_NODE_RUN_NOT_VALID);
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private void setAuthorization(WebClient.RequestBodySpec requestSpec, EndpointWithSystem endpointWithSystem) {
        Optional<Authorization> authorizationOpt = Optional.ofNullable(endpointWithSystem.getSystem()).map(IntegrationSystem::getAuthorization);
        if (authorizationOpt.isPresent()) {
            authFunctions.get(authorizationOpt.get().getType()).accept(requestSpec, authorizationOpt.get(), authConfig);
        }
    }

}
