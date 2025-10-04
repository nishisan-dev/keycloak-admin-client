package dev.nishisan.keycloak.admin.client.management;

import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import dev.nishisan.keycloak.admin.client.exception.CreateRoleException;
import dev.nishisan.keycloak.admin.client.types.RealmRole;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Manages operations related to Keycloak realms.
 * Provides functionality for creating and managing roles within a specified Keycloak realm.
 */
public class RealmManager extends BaseManager {
    public RealmManager(OkHttpClient httpClient, SSOConfig config) {
        super(httpClient, config);
    }

    /**
     * Creates a new Realm Role and returns the created role with its id populated.
     *
     * @param role The role payload to create
     * @return the same role instance with id populated
     * @throws IOException on network errors
     * @throws CreateRoleException when Keycloak rejects the creation
     */
    public RealmRole createRole(RealmRole role) throws IOException, CreateRoleException {
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
            throw new CreateRoleException(ex);
        }
    }
}
