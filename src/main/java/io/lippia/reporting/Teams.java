package io.lippia.reporting;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Teams implements Notifier {
    private final String WEBHOOK_URL = System.getenv().get("WEBHOOK_URL_TEAMS");

    @Override
    public void sendMessage(String message) throws Exception {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            throw new IllegalStateException("WEBHOOK_URL_TEAMS is not set. Please set the WEBHOOK_URL_TEAMS environment variable.");
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
