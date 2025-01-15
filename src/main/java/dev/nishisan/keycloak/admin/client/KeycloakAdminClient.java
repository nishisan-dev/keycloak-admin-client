/*
 * Copyright (C) 2025 Lucas Nishimura < lucas at nishisan.dev > 
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
package dev.nishisan.keycloak.admin.client;

import dev.nishisan.keycloak.admin.client.auth.KeyCloakOAuthClientManager;
import dev.nishisan.keycloak.admin.client.config.SSOConfig;

/**
 * Simple Client for Keycloak admin
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class KeycloakAdminClient {

    private final SSOConfig config;
    private KeyCloakOAuthClientManager tokenManager;
    

    public KeycloakAdminClient(SSOConfig config) {
        this.config = config;
        this.tokenManager = new KeyCloakOAuthClientManager(config);
    }

    public KeycloakAdminClient(String clientId, String clientSecret, String realm, String baseUrl) {
        this.config = new SSOConfig(clientId, clientSecret, realm, baseUrl);
        this.tokenManager = new KeyCloakOAuthClientManager(config);        
    }

    public KeyCloakOAuthClientManager getTokenManager() {
        return tokenManager;
    }

    
    
    
}
