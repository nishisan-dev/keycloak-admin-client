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
package dev.nishisan.keycloak.admin.client.management;

import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import dev.nishisan.keycloak.admin.client.exception.CreateUserException;
import dev.nishisan.keycloak.admin.client.types.User;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class UserManager extends BaseManager {

    public UserManager(SSOConfig config, OkHttpClient httpClient) {
        super(httpClient, config);
    }

    /**
     * Creates a new SSO User
     *
     * @param user
     * @throws IOException
     * @throws CreateUserException
     */
    public void createUser(User user) throws IOException, CreateUserException {
        /**
         * The target URL
         */
        String targetUrl = this.config.getBaseUrl() + "/admin/realms/" + this.config.getRealm() + "/users";

        try {
            Response r = this.postJson(targetUrl, user);
        } catch (IOException ex) {
            throw new CreateUserException(ex);
        }

    }

}
