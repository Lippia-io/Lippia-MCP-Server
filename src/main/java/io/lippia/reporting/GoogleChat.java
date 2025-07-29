package io.lippia.reporting;

import okhttp3.*;

public class GoogleChat implements Notifier {
    private final String WEBHOOK_URL = System.getenv().get("WEBHOOK_URL_GOOGLE");

    @Override
    public void sendMessage(String message) throws Exception {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            throw new IllegalStateException("WEBHOOK_URL_GOOGLE is not set. Please set the WEBHOOK_URL_GOOGLE environment variable.");
        }

        String json = String.format("{\"text\": \"%s\"}", message);
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Webhook.send(WEBHOOK_URL, body);
    }

    @Override
    public String getWebhookUrl() {
        return this.WEBHOOK_URL;
    }
}
