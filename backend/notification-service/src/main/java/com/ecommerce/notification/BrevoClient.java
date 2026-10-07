package com.ecommerce.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends email through Brevo's HTTPS API. Useful where outbound SMTP ports are blocked,
 * such as free hosting plans. Enabled by setting BREVO_API_KEY.
 */
@Component
public class BrevoClient {

    private final RestClient http;
    private final String apiKey;
    private final String url;

    public BrevoClient(RestClient.Builder builder,
                       @Value("${app.mail.brevo-api-key:}") String apiKey,
                       @Value("${app.mail.brevo-url:https://api.brevo.com/v3/smtp/email}") String url) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.http = builder.requestFactory(requestFactory).build();
        this.apiKey = apiKey.trim();
        this.url = url;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    public void send(String fromEmail, String fromName, String to, String subject, String html, String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sender", Map.of("name", fromName, "email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", html);
        if (text != null) {
            body.put("textContent", text);
        }
        http.post()
                .uri(url)
                .header("api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
