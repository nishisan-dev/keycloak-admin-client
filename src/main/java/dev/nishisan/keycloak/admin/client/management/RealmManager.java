package dev.nishisan.keycloak.admin.client.management;

import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import dev.nishisan.keycloak.admin.client.exception.CreateRoleException;
import dev.nishisan.keycloak.admin.client.types.RealmRole;
import dev.nishisan.keycloak.admin.client.exception.SSOIOException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Manages operations related to Keycloak realms.
 * Provides functionality for creating and managing roles within a specified Keycloak realm.
 */
public class RealmManager extends BaseManager {
    public RealmManager( SSOConfig config,OkHttpClient httpClient) {
        super(httpClient, config);
    }

    /**
     * Creates a new Realm Role and returns the created role with its id populated.
     *
     * @param role The role payload to create
     * @return the same role instance with id populated
     * @throws SSOIOException on network errors
     * @throws CreateRoleException when Keycloak rejects the creation
     */
    public RealmRole createRole(RealmRole role) throws SSOIOException, CreateRoleException {
        String targetUrl = this.config.getBaseUrl() + "/admin/realms/" + this.config.getRealm() + "/roles";
        try (Response r = this.postJson(targetUrl, role)) {
            int code = r.code();
            if (code != 201) {
                throw new CreateRoleException("Failed to create realm role. HTTP Status: " + code);
            }
            String location = r.header("Location");
            if (location != null && !location.isEmpty()) {
                String[] parts = location.split("/");
                String id = parts[parts.length - 1];
                role.setId(id);
            }
            return role;
        } catch (IOException ex) {
            throw new SSOIOException(ex);
        }
    }

    /**
     * Lists all realm roles.
     * @return list of realm roles (possibly empty)
     * @throws SSOIOException on network errors
     */
    public List<RealmRole> listRoles() throws SSOIOException {
        String base = this.config.getBaseUrl();
        HttpUrl url = HttpUrl.parse(base)
                .newBuilder()
                .addPathSegments("admin/realms")
                .addPathSegment(this.config.getRealm())
                .addPathSegment("roles")
                .build();
        try {
            try (Response r = this.get(url.toString())) {
                if (!r.isSuccessful()) {
                    return Collections.emptyList();
                }
                String json = r.body() != null ? r.body().string() : "";
                if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                    return Collections.emptyList();
                }
                RealmRole[] arr = this.gson().fromJson(json, RealmRole[].class);
                if (arr == null || arr.length == 0) return Collections.emptyList();
                return Arrays.asList(arr);
            }
        } catch (IOException ex) {
            throw new SSOIOException(ex);
        }
    }

    /**
     * Gets a realm role by its name. Returns null if not found.
     * Keycloak returns a RoleRepresentation with id and other fields when found.
     * @param roleName the role name
     * @return RealmRole or null
     * @throws SSOIOException on network errors
     */
    public RealmRole getRoleByName(String roleName) throws SSOIOException {
        if (roleName == null || roleName.isBlank()) return null;
        HttpUrl url = HttpUrl.parse(this.config.getBaseUrl())
                .newBuilder()
                .addPathSegments("admin/realms")
                .addPathSegment(this.config.getRealm())
                .addPathSegment("roles")
                .addPathSegment(roleName)
                .build();
        try {
            try (Response r = this.get(url.toString())) {
                if (!r.isSuccessful()) {
                    return null;
                }
                String json = r.body() != null ? r.body().string() : "";
                if (json == null || json.trim().isEmpty()) {
                    return null;
                }
                return this.gson().fromJson(json, RealmRole.class);
            }
        } catch (IOException ex) {
            throw new SSOIOException(ex);
        }
    }

    /**
     * Deletes a realm role by its name.
     * @param roleName role name
     * @return true if the role was deleted (204)
     * @throws SSOIOException on network errors
     */
    public boolean deleteRole(String roleName) throws SSOIOException {
        if (roleName == null || roleName.isBlank()) return false;
        HttpUrl url = HttpUrl.parse(this.config.getBaseUrl())
                .newBuilder()
                .addPathSegments("admin/realms")
                .addPathSegment(this.config.getRealm())
                .addPathSegment("roles")
                .addPathSegment(roleName)
                .build();
        try {
            try (Response r = this.delete(url.toString())) {
                return r.code() == 204;
            }
        } catch (IOException ex) {
            throw new SSOIOException(ex);
        }
    }
}
