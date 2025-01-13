/*
 * Copyright (C) 2023 Lucas Nishimura <lucas.nishimura@gmail.com>
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
package dev.nishisan.keycloak.admin.client.http;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Lucas Nishimura <lucas.nishimura@gmail.com>
 * @created 05.01.2023
 */
public class CustomHttpRequestInitializer implements HttpRequestInitializer {

    private Map<String, String> appendHeaders = new ConcurrentHashMap<>();

    public CustomHttpRequestInitializer() {
    }

    public CustomHttpRequestInitializer(Map<String, String> appendHeaders) {
        this.appendHeaders = appendHeaders;
    }

    @Override
    public void initialize(HttpRequest request) throws IOException {
        if (request.getHeaders() != null) {
            request.getHeaders().putAll(appendHeaders);
        }
    }

}
