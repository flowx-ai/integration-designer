package ai.flowx.integration.service;

import ai.flowx.integration.config.AuthConfig;
import ai.flowx.integration.domain.Authorization;
import lombok.extern.apachecommons.CommonsLog;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class AuthorizationTokenService {
    public String getServiceAccountToken(Authorization authorization, AuthConfig authConfig) {
        String realm = Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("realm")).map(Object::toString).orElse(null);
        String clientId = Optional.ofNullable(authorization.getConfiguration()).map(conf -> conf.get("clientId")).map(Object::toString).orElse(null);
        String clientSecret = Optional.ofNullable(authConfig.getClients()).flatMap(list -> list.stream().filter(client -> Objects.equals(client.getRealm(), realm) && Objects.equals(client.getClientId(), clientId)).findFirst())
                .map(clientConfig -> clientConfig.getClientSecret()).orElse(null);

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authConfig.getServerUrl())
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();
        return tokenResponse.getToken();
    }
}
