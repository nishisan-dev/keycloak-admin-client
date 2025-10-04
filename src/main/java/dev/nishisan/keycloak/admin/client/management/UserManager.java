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
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;

/**
 * UserManager provides functionality to manage users in a Keycloak SSO environment.
 * This class allows creating users, retrieving user information, updating user details,
 * and performing user-related operations through Keycloak's REST API.
 */
public class UserManager extends BaseManager {

    public UserManager(SSOConfig config, OkHttpClient httpClient) {
        super(httpClient, config);
    }

    /**
     * Creates a new SSO User and returns the created user with its UID (id) set.
     *
     * @param user The user payload to create
     * @return the same user instance with id populated
     * @throws IOException on network errors
     * @throws CreateUserException when Keycloak rejects the creation
     */
    public User createUser(User user) throws IOException, CreateUserException {
        // Target URL
        String targetUrl = this.config.getBaseUrl() + "/admin/realms/" + this.config.getRealm() + "/users";

        try (Response r = this.postJson(targetUrl, user)) {
            int code = r.code();
            if (code != 201) {
                throw new CreateUserException("Failed to create user. HTTP Status: " + code);
            }
            String location = r.header("Location");
            if (location != null && !location.isEmpty()) {
                // Extract id from the Location header (last path segment)
                String[] parts = location.split("/");
                String id = parts[parts.length - 1];
                user.setId(id);
            }
            return user;
        } catch (IOException ex) {
            throw new CreateUserException(ex);
        }
    }

    /**
     * Finds a user by username or email. Returns the first exact match or null if not found.
     * @param usernameOrEmail the username or email to search for
     * @return User or null
     * @throws IOException on network errors
     */
    public User findUser(String usernameOrEmail) throws IOException {
        String queryKey = usernameOrEmail.contains("@") ? "email" : "username";
        String url = this.config.getBaseUrl()
                + "/admin/realms/" + this.config.getRealm()
                + "/users?" + queryKey + "=" + okhttp3.HttpUrl.parse("http://x/" + usernameOrEmail).encodedPath().substring(1)
                + "&exact=true";
        try (Response r = this.get(url)) {
            if (!r.isSuccessful()) {
                return null;
            }
            String json = r.body() != null ? r.body().string() : "";
            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                return null;
            }
            User[] users = this.gson().fromJson(json, User[].class);
            if (users != null && users.length > 0) {
                return users[0];
            }
            return null;
        }
    }

    /**
     * Changes a user's password.
     * @param userId Keycloak user id
     * @param newPassword new password value
     * @param temporary whether the new password is temporary
     * @return true if password was changed (204 status)
     * @throws IOException on network errors
     */
    public boolean changePassword(String userId, String newPassword, boolean temporary) throws IOException {
        String url = this.config.getBaseUrl() + "/admin/realms/" + this.config.getRealm() + "/users/" + userId + "/reset-password";
        dev.nishisan.keycloak.admin.client.types.Credentials payload =
                new dev.nishisan.keycloak.admin.client.types.Credentials("password", newPassword, temporary);
        try (Response r = this.putJson(url, payload)) {
            return r.code() == 204;
        }
    }

    /**
     * Updates a user's email address.
     * @param userId user id
     * @param newEmail new email
     * @return true if update succeeded (204 status)
     * @throws IOException on network errors
     */
    public boolean updateEmail(String userId, String newEmail) throws IOException {
        String url = this.config.getBaseUrl() + "/admin/realms/" + this.config.getRealm() + "/users/" + userId;
        // Minimal payload to update email
        class EmailUpdate { String email; Boolean emailVerified; EmailUpdate(String e){ this.email=e; this.emailVerified=false; } }
        EmailUpdate payload = new EmailUpdate(newEmail);
        try (Response r = this.putJson(url, payload)) {
            return r.code() == 204;
        }
    }

}
