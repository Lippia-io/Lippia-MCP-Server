package com.crowdar.utils;

import com.crowdar.models.Features;
import com.crowdar.models.requests.PromptFeatureRequest;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PromptBuilder {
    private static final WebClient webClient = WebClient.create(
            System.getenv().getOrDefault("PROMPT_SERVICE_BASE_URL", "http://localhost:8080"));
    private static final String FEATURE_ENDPOINT = "/template/features";
    private static final String STEPS_ENDPOINT = "/template/steps";

    public static String build(final String uri, Object body) {
        try {
            return webClient.post()
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
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
