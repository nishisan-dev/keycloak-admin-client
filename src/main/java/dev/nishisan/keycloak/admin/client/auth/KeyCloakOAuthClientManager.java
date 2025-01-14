/*
 * Copyright (C) 2025 Lucas Nishimura <lucas.nishimura at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.nishisan.keycloak.admin.client.auth;

import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import dev.nishisan.keycloak.admin.client.http.CustomHttpRequestInitializer;
import java.io.IOException;

/**
 * Handles The Keycloak Token Management
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 12.01.2025
 */
public class KeyCloakOAuthClientManager {

    private final SSOConfig config;
    private final JsonFactory JSON_FACTORY = new GsonFactory();

    private TokenResponseWrapper currentToken;

    public KeyCloakOAuthClientManager(SSOConfig config) {
        this.config = config;
    }

    /**
     * Get the Admin Client Token
     *
     * @return
     * @throws IOException
     */
    private TokenResponseWrapper generateToken() throws IOException {
        if (this.currentToken != null) {
            if (this.currentToken.isExpired()) {
                try {
                    return this.refreshToken();
                } catch (IOException ex) {
                    //
                    // Failed will try to issue a new one..
                    //
                    this.currentToken = null;
                }
            } else {
                //
                // Reuse the previous token
                //
                return this.currentToken;
            }
        }
        //
        // Issue a new Token
        //
        GenericUrl url = new GenericUrl(this.config.getTokenUrl());
        ClientCredentialsTokenRequest clientTokenRequest
                = new ClientCredentialsTokenRequest(new NetHttpTransport(),
                        JSON_FACTORY, url);
        clientTokenRequest.setGrantType("client_credentials");
        clientTokenRequest.setClientAuthentication(new BasicAuthentication(config.getClientId(), config.getClientSecret()));
        clientTokenRequest.setRequestInitializer(new CustomHttpRequestInitializer(this.config.getExtraHeaders()));
        TokenResponse response = clientTokenRequest.execute();
        return new TokenResponseWrapper(response);
    }

    /**
     * Refresh The Token
     *
     * @return
     * @throws IOException
     */
    private TokenResponseWrapper refreshToken() throws IOException {
        GenericUrl url = new GenericUrl(this.config.getTokenUrl());
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(new NetHttpTransport(), JSON_FACTORY, url, this.currentToken.getRefreshToken());
        TokenResponse a = refreshTokenRequest.execute();
        this.currentToken = new TokenResponseWrapper(a);
        return this.currentToken;
    }

    public TokenResponseWrapper getToken() throws IOException {
        if (this.currentToken == null) {
            return this.generateToken();
        }
        return this.currentToken;
    }
}
