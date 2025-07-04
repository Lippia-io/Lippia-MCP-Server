package io.lippia.utils;

import io.lippia.models.Features;
import io.lippia.models.requests.PromptFeatureRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class PromptBuilder {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = System.getenv().getOrDefault("PROMPT_SERVICE_BASE_URL", "http://localhost:8080");
    private static final String FEATURE_ENDPOINT = "/template/features";
    private static final String STEPS_ENDPOINT = "/template/steps";

    public static String build(final String uri, Object body) {
        String url = BASE_URL + uri;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Error HTTP " + e.getStatusText() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error building prompt for " + uri, e);
        }
    }

    public static String buildPromptForFeatures(PromptFeatureRequest body) {
        return build(FEATURE_ENDPOINT, body);
    }

    public static String buildPromptForSteps(Features body) {
        return build(STEPS_ENDPOINT, body);
    }
}
