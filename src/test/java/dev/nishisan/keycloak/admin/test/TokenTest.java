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
package dev.nishisan.keycloak.admin.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.nishisan.keycloak.admin.client.KeycloakAdminClient;
import dev.nishisan.keycloak.admin.client.auth.TokenResponseWrapper;
import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class TokenTest {

    /**
     * Try to Get a Admin TOKEN
     *
     * @param args
     */
    public static void main(String[] args) {
        SSOConfig config;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            config = mapper.readValue(new File("config/sample.yaml"), SSOConfig.class);

            KeycloakAdminClient client = new KeycloakAdminClient(config);
            TokenResponseWrapper t = client.getTokenManager().getToken();
            System.out.println("Token OK:" + t.getAccessToken());
        } catch (IOException ex) {
            Logger.getLogger(TokenTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
