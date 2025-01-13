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

import com.google.api.client.auth.oauth2.TokenResponse;
import java.time.Instant;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura at gmail.com>
 * created 12.01.2025
 */
public class TokenResponseWrapper {

    private final TokenResponse response;
    private Instant expirationTime;

    public TokenResponseWrapper(TokenResponse response) {
        this.response = response;
        Instant now = Instant.now();
        if (response.getExpiresInSeconds() != null) {
            if (response.getExpiresInSeconds() > 0L) {
                this.expirationTime = now.plusSeconds(response.getExpiresInSeconds());
            }
        }
    }

    public boolean isExpired() {
        return !Instant.now().isBefore(expirationTime);
    }

    public String getAccessToken() {
        return this.response.getAccessToken();
    }

    public String getRefreshToken() {
        return this.response.getRefreshToken();
    }
}
