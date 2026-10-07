package com.ecommerce.inventory;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Talks to a real local HTTP server: PATCH is not supported by every HTTP client implementation.
 */
class ProductStockClientTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256!!";

    private HttpServer server;
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            method.set(exchange.getRequestMethod());
            path.set(exchange.getRequestURI().getPath());
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void sendsAuthenticatedPatchToStockEndpoint() {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/api/products/";
        ProductStockClient client = new ProductStockClient(RestClient.builder(), baseUrl, SECRET);

        client.adjust("p-42", -3);

        assertThat(method.get()).isEqualTo("PATCH");
        assertThat(path.get()).isEqualTo("/api/products/p-42/stock");
        assertThat(body.get()).contains("\"delta\":-3");
        String token = auth.get().substring("Bearer ".length());
        String role = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
        assertThat(role).isEqualTo("ADMIN");
    }
}
