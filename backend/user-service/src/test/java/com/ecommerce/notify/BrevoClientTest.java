package com.ecommerce.notify;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class BrevoClientTest {

    private HttpServer server;
    private final AtomicReference<String> apiKey = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v3/smtp/email", exchange -> {
            apiKey.set(exchange.getRequestHeaders().getFirst("api-key"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"messageId\":\"<id@brevo>\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
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
    void postsTransactionalEmailWithApiKey() {
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email";
        BrevoClient client = new BrevoClient(RestClient.builder(), "xkeysib-test", url);

        client.send("shop@example.com", "AxeDrobe", "riya@example.com", "123456 is your code", "<p>123456</p>", "123456");

        assertThat(client.isConfigured()).isTrue();
        assertThat(apiKey.get()).isEqualTo("xkeysib-test");
        assertThat(body.get())
                .contains("\"sender\":{\"name\":\"AxeDrobe\",\"email\":\"shop@example.com\"}")
                .contains("\"to\":[{\"email\":\"riya@example.com\"}]")
                .contains("\"subject\":\"123456 is your code\"")
                .contains("\"htmlContent\":\"<p>123456</p>\"");
    }

    @Test
    void disabledWithoutKey() {
        assertThat(new BrevoClient(RestClient.builder(), " ", "http://unused").isConfigured()).isFalse();
    }
}
