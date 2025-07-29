package io.lippia.utils;

import okhttp3.*;

import java.io.IOException;

public class GoogleChatNotifier {
    private static final String WEBHOOK_URL = System.getenv().get("GS_WEBHOOK_URL");

    public static void sendMessage(String message) throws IOException {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isEmpty()) {
            throw new IllegalStateException("WEBHOOK_URL is not set. Please set the PROMPT_SERVICE_BASE_URL environment variable.");
        }

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String json = String.format("{\"text\": \"%s\"}", message);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(WEBHOOK_URL)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Fallo al enviar mensaje: " + response);
            }
        }
    }
}
