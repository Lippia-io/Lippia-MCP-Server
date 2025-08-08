package io.lippia.reporting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

public class Slack implements Notifier {
    private final String WEBHOOK_URL = System.getenv().get("WEBHOOK_URL_SLACK");

    @Override
    public void sendMessage(String message) throws Exception {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            throw new IllegalStateException("WEBHOOK_URL_SLACK is not set. Please set the WEBHOOK_URL_SLACK environment variable.");
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", message);
        String json = new Gson().toJson(jsonObject);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Webhook.send(WEBHOOK_URL, body);
    }

    @Override
    public String getWebhookUrl() {
        return this.WEBHOOK_URL;
    }
}
