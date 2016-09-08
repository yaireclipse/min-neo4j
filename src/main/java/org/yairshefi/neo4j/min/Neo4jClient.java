package org.yairshefi.neo4j.min;

import java.io.IOException;
import java.util.Base64;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by yairshefi on 9/8/16.
 */
public class Neo4jClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 7474;
    public static final String DEFAULT_USER = "neo4j";
    public static final String DEFAULT_PASSWORD = "neo4j";
    private final CredentialsProvider credentialsProvider;
    private final CloseableHttpClient httpClient;
    private final String host;
    private final Integer port;
    private final String user;
    private final String password;

    public static class Builder {
        String host;
        Integer port;
        String user;
        String password;
        public Builder host(final String host) {
            this.host = host;
            return this;
        }
        public Builder port(final Integer port) {
            this.port = port;
            return this;
        }
        public Builder user(final String user) {
            this.user = user;
            return this;
        }
        public Builder password(final String password) {
            this.password = password;
            return this;
        }
        public Neo4jClient build() {
            return new Neo4jClient(host, port, user, password);
        }
    }

    private Neo4jClient(final String host, final Integer port, final String user, final String password) {

        this.host = host != null ? host : DEFAULT_HOST;
        this.port = port != null ? port : DEFAULT_PORT;
        this.user = user != null ? user : DEFAULT_USER;
        this.password = password != null ? password : DEFAULT_PASSWORD;

        credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.user, this.password));
        httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
    }

    public JSONObject post(final JSONObject rootJson) throws IOException {

        final String url = String.format("http://%s:%s/db/data/transaction/commit", host, port);
        final HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json; charset=UTF-8");
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
//        httpPost.addHeader("X-Stream", "true"); didn't notice any change in performance; perhaps it's good only for really big JSONs
        final String userPassword = String.format("%s:%s", user, password);
        httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(userPassword.getBytes()));

        final StringEntity stringEntity = new StringEntity(rootJson.toString(), "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        final CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        final String restResponse = EntityUtils.toString(httpResponse.getEntity());

        System.out.println(restResponse);
        final JSONObject jsonObject = new JSONObject(restResponse);
        System.out.println(jsonObject);
        return jsonObject;
    }
}
