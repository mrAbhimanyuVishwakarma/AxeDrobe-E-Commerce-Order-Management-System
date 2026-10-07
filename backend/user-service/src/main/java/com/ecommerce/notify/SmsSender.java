package com.ecommerce.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Sends sign-in codes by SMS to Indian mobile numbers through 2Factor.in or Fast2SMS.
 * Pick the provider with SMS_PROVIDER (twofactor | fast2sms) and set SMS_API_KEY.
 */
@Slf4j
@Component
public class SmsSender {

    private final RestClient http;
    private final String provider;
    private final String apiKey;
    private final String template;

    public SmsSender(RestClient.Builder builder,
                     @Value("${app.sms.provider:}") String provider,
                     @Value("${app.sms.api-key:}") String apiKey,
                     @Value("${app.sms.template:}") String template) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.http = builder.requestFactory(requestFactory).build();
        this.provider = provider.trim().toLowerCase(Locale.ROOT);
        this.apiKey = apiKey.trim();
        this.template = template.trim();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(provider) && StringUtils.hasText(apiKey);
    }

    /**
     * @param mobile 10-digit Indian mobile number
     */
    public void sendOtp(String mobile, String code) {
        try {
            switch (provider) {
                case "twofactor", "2factor" -> sendWithTwoFactor(mobile, code);
                case "fast2sms" -> sendWithFast2Sms(mobile, code);
                default -> throw new IllegalStateException("Unknown SMS_PROVIDER '" + provider + "'");
            }
        } catch (RestClientException | IllegalStateException e) {
            log.error("Could not send SMS through {}: {}", provider, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "We couldn't send the SMS right now. Please try again or use your email.");
        }
    }

    // https://2factor.in/API/V1/{api_key}/SMS/{phone}/{otp}/{template}
    private void sendWithTwoFactor(String mobile, String code) {
        String url = "https://2factor.in/API/V1/{key}/SMS/{phone}/{otp}"
                + (StringUtils.hasText(template) ? "/{template}" : "");
        Map<?, ?> response = http.post()
                .uri(url, apiKey, "+91" + mobile, code, template)
                .retrieve()
                .body(Map.class);
        if (response == null || !"Success".equalsIgnoreCase(String.valueOf(response.get("Status")))) {
            throw new IllegalStateException("2Factor rejected the request: " + response);
        }
    }

    // https://www.fast2sms.com/dev/bulkV2 with route=otp
    private void sendWithFast2Sms(String mobile, String code) {
        Map<?, ?> response = http.get()
                .uri("https://www.fast2sms.com/dev/bulkV2?authorization={key}&route=otp&variables_values={otp}&numbers={phone}",
                        apiKey, code, mobile)
                .retrieve()
                .body(Map.class);
        if (response == null || !Boolean.TRUE.equals(response.get("return"))) {
            throw new IllegalStateException("Fast2SMS rejected the request: " + response);
        }
    }
}
