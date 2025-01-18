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
import dev.nishisan.keycloak.admin.client.events.ITokenEventListener;
import dev.nishisan.keycloak.admin.client.events.SafeEventListener;
import dev.nishisan.keycloak.admin.client.http.CustomHttpRequestInitializer;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles The Keycloak Token Management
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 12.01.2025
 */
public class KeyCloakOAuthClientManager {

    private final SSOConfig config;
    private final JsonFactory JSON_FACTORY = new GsonFactory();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private TokenResponseWrapper currentToken;
    private Map<String, SafeEventListener> listeners = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(KeyCloakOAuthClientManager.class);
    private AtomicBoolean runing = new AtomicBoolean(true);

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
        TokenResponse tokenResponse = clientTokenRequest.execute();

        TokenResponseWrapper response = new TokenResponseWrapper(tokenResponse);
        logger.debug("Token Issued");
        long milisBefore = 300;
        Instant executionTime = response.getExpirantionTime().minusMillis(milisBefore);
        long delay = Duration.between(Instant.now(), executionTime).toMillis();
        scheduler.schedule(new TokenManagementThread(), delay, TimeUnit.MILLISECONDS);
        logger.debug("Refresh Scheduled for:{} ms", delay);
        listeners.forEach((k, v) -> {
            v.onTokenIssued(response);
        });
        return response;
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
        //
        // Prevents loop
        //        
        this.currentToken = null;
        TokenResponse a = refreshTokenRequest.execute();
        this.currentToken = new TokenResponseWrapper(a);
        logger.debug("Token Refreshed");
        listeners.forEach((k, v) -> {
            v.onTokenRefreshed(this.currentToken);
        });
        return this.currentToken;
    }

    public TokenResponseWrapper getToken() throws IOException {
        if (this.currentToken == null) {
            return this.generateToken();
        }
        return this.currentToken;
    }

    private class TokenManagementThread implements Runnable {

        @Override
        public void run() {
            if (runing.get()) {
                try {

                    logger.debug("Refreshing Token");
                    /**
                     * De fato renova o token :)
                     */
                    generateToken();
                } catch (IOException ex) {
                    logger.error("Failed to Refresh Token", ex);
                }
            }
        }
    }

    public void shutdown() {
        this.runing.set(false);
        this.scheduler.shutdown();
    }

    public void registerListener(ITokenEventListener listener) {
        SafeEventListener safeListener = new SafeEventListener(listener);
        this.listeners.put(listener.getUniqueName(), safeListener);
    }

}
