package ai.flowx.integration.service;

import ai.flowx.integration.config.AuthConfig;
import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.TestEndpointResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
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

@RequiredArgsConstructor
@Component
public class TestRunEndpointService {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");
    private static final String EMPTY_STRING = "";
    private final WebClient webClient;
    private final AuthConfig authConfig;
    private final EndpointService endpointService;
    private final AuthorizationTokenService authorizationTokenService;
    private final ObjectMapper objectMapper;
    private Map<AuthorizationType, TriConsumer<WebClient.RequestBodySpec, Authorization, AuthConfig>> authFunctions = new HashMap<>();

    @PostConstruct
    public void initAuthFunctions(){
        authFunctions = Map.of(
                AuthorizationType.BASIC, (requestSpec, authorization, authConfig) -> requestSpec.headers(httpHeaders -> httpHeaders.setBasicAuth(Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("username")).map(Object::toString).orElse(null), Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("password")).map(Object::toString).orElse(null))),
                AuthorizationType.SERVICE_ACCOUNT, (requestSpec, authorization, authConfig) -> requestSpec.headers(httpHeaders -> httpHeaders.setBearerAuth(authorizationTokenService.getServiceAccountToken(authorization, authConfig)))
        );
    }

    @SneakyThrows
    public Mono<TestEndpointResponseDTO> executeRequest(String endpointId) {
        EndpointWithSystem endpoint = endpointService.getEndpointWithSystem(endpointId);
        Map<String, Object> values = new HashMap<>();

        URI uri = getUri(endpoint, values);

        WebClient.RequestBodySpec requestSpec = webClient
                .method(endpoint.getHttpMethod())
                .uri(uri.toString(), uriBuilder -> {
                    if (!CollectionUtils.isEmpty(endpoint.getQueryParameters())) {
                        endpoint.getQueryParameters()
                                .forEach(qp -> uriBuilder.queryParam(qp.getKey(), qp.getDefaultValue()));
                    }
                    return uriBuilder.build();
                });

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
                                .curlCommand( responseDTO.getCurlCommand())
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

    private URI getUri(EndpointWithSystem endpoint, Map<String, Object> values) throws
            MalformedURLException, URISyntaxException {
        URL url = new URL(processUrl(endpoint, values));
        // Convert URL to URI
        return url.toURI();
    }

    private String processUrl(EndpointWithSystem endpoint, Map<String, Object> values) {
        Optional<String> rawBaseUrl = Optional.ofNullable(endpoint.getSystem().getBaseUrl());
        if (rawBaseUrl.isPresent()) {
            String systemUrl = replacePlaceholder(rawBaseUrl.get(), values);
            String endpointUrl = replacePlaceholder(endpoint.getUrl(), getValues(endpoint.getPathParameters()));
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

    public String replacePlaceholder(String originalString, Map<String, Object> values) {
        if (StringUtils.isEmpty(originalString)) {
            return EMPTY_STRING;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(originalString);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String replacement = Objects.toString(PropertyUtils.getProperty(values, matcher.group(1)), null);
            if (replacement != null) {
                matcher.appendReplacement(sb, replacement);
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
