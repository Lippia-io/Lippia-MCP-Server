package io.lippia.reporting;

import okhttp3.*;

public class Discord implements Notifier {
    private final String WEBHOOK_URL = System.getenv().get("WEBHOOK_URL_DISCORD");

    @Override
    public void sendMessage(String message) throws Exception {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            throw new IllegalStateException("WEBHOOK_URL_DISCORD is not set. Please set the WEBHOOK_URL_DISCORD environment variable.");
        }

        String json = String.format("{\"content\": \"%s\"}", message);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Webhook.send(WEBHOOK_URL, body);
    }

    @Override
    public String getWebhookUrl() {
        return this.WEBHOOK_URL;
    }
}
