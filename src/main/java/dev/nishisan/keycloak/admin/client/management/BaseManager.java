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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class BaseManager {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected final OkHttpClient httpClient;
    protected final SSOConfig config;
    protected final Logger logger = LoggerFactory.getLogger(BaseManager.class);

    public BaseManager(OkHttpClient httpClient, SSOConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public Gson gson() {
        return this.gson;
    }

    public Response postJson(String url, Object payload) throws IOException {
        Request.Builder builder = new Request.Builder();
        String jsonPayLoad = this.gson().toJson(payload);
        RequestBody body = RequestBody.create(jsonPayLoad, MediaType.parse("application/json"));
        builder.url(url).method("POST", body);
        Request req = builder.build();
        logger.debug("POST: {}, Payload:{}", req.url(), jsonPayLoad);
        Response r = this.httpClient.newCall(req).execute();
        return r;
    }

    public Response putJson(String url, Object payload) throws IOException {
        Request.Builder builder = new Request.Builder();
        String jsonPayLoad = this.gson().toJson(payload);
        RequestBody body = RequestBody.create(jsonPayLoad, MediaType.parse("application/json"));
        builder.url(url).method("PUT", body);
        Request req = builder.build();
        logger.debug("PUT: {}, Payload:{}", req.url(), jsonPayLoad);
        return this.httpClient.newCall(req).execute();
    }

    public Response get(String url) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(url).get();
        Request req = builder.build();
        logger.debug("GET: {}", req.url());
        return this.httpClient.newCall(req).execute();
    }

    public Response deleteJson(String url, Object payload) throws IOException {
        Request.Builder builder = new Request.Builder();
        String jsonPayLoad = this.gson().toJson(payload);
        RequestBody body = RequestBody.create(jsonPayLoad, MediaType.parse("application/json"));
        builder.url(url).method("DELETE", body);
        Request req = builder.build();
        logger.debug("DELETE: {}, Payload:{}", req.url(), jsonPayLoad);
        return this.httpClient.newCall(req).execute();
    }

    public Response delete(String url) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(url).delete();
        Request req = builder.build();
        logger.debug("DELETE: {}", req.url());
        return this.httpClient.newCall(req).execute();
    }
}
