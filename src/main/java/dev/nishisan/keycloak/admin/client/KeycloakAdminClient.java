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
import dev.nishisan.keycloak.admin.client.management.RealmManager;
import dev.nishisan.keycloak.admin.client.management.UserManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Client for Keycloak admin
 *
 * @author Lucas Nishimura < lucas at nishisan.dev >
 */
public class KeycloakAdminClient {

    private final SSOConfig config;
    private KeyCloakOAuthClientManager tokenManager;
    private OkHttpClient httpClient;
    private UserManager userManager;
    private RealmManager realmManager;
    private final Logger logger = LoggerFactory.getLogger(KeycloakAdminClient.class);

    public KeycloakAdminClient(SSOConfig config) {
        this.config = config;
        this.tokenManager = new KeyCloakOAuthClientManager(config);
        this.initHttpClient();
        this.initManagers();
    }

    public KeycloakAdminClient(String clientId, String clientSecret, String realm, String baseUrl) {
        this.config = new SSOConfig(clientId, clientSecret, realm, baseUrl);
        this.tokenManager = new KeyCloakOAuthClientManager(config);
        this.initHttpClient();
        this.initManagers();
    }

    private void initManagers() {
        this.userManager = new UserManager(config, httpClient);
        this.realmManager = new RealmManager(config,httpClient);
    }

    public KeyCloakOAuthClientManager getTokenManager() {
        return tokenManager;
    }

    private void initHttpClient() {
        try {
            //
            //  Delega um TrustManager para aceitar todos os certificados
            //
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            this.httpClient = new OkHttpClient.Builder().addInterceptor((chain) -> {
                Request original = chain.request();

                /**
                 * Monta um novo request no interceptor para poder adicionar o
                 * cabeçaho de autenticação
                 */
                Request newRequest = original.newBuilder()
                        .header("Authorization", "Bearer " + this.tokenManager.getToken().getAccessToken())
                        .build();

                logger.debug("Interceptor Called Authenticated");

                if (newRequest.body() != null) {
                    if (newRequest.body().contentLength() > 0) {
                        logger.debug("Body contentLength :[{}]", newRequest.body().contentLength());
                    }
                }

                logger.debug("Target: Authenticated URL:[{}] Method:[{}]", newRequest.url().uri(), newRequest.method());
                logger.debug("Dumping Upstream Request Headers");
                for (String header : newRequest.headers().names()) {
                    logger.debug("Header OUT: [{}]:=[{}]", header, newRequest.header(header));
                }
                logger.debug("Done Dumping");
                return chain.proceed(newRequest);
            }).sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    //
                    // Set um hostNameVerifier para aceitar qualquer relação DOMAIN/Certificado
                    //
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .retryOnConnectionFailure(true)
                    .build();

        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            logger.error("Failed to Set SSL Context", ex);
        }
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public RealmManager getRealmManager() {
        return this.realmManager;
    }

}
