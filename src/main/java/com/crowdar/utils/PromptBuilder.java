package com.crowdar.utils;

import com.crowdar.models.Flow;
import com.crowdar.models.requests.PromptRequest;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class PromptBuilder {
    private static final WebClient webClient = WebClient.create(
            System.getenv().getOrDefault("PROMPT_SERVICE_BASE_URL", "http://localhost:8080"));
    private static final String ENDPOINT = "/template";

    public static String buildPromptFromActionsForWeb(Flow flow) {
        try {
            PromptRequest request = convertFlowToPromptRequest(flow);

            return webClient.post()
                    .uri(ENDPOINT)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error HTTP " + e.getStatusText() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error building prompt from flow", e);
        }
    }

    private static PromptRequest convertFlowToPromptRequest(Flow flow) {
        return new PromptRequest("web", flow.flow());
    }
}
