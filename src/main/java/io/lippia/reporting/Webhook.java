package io.lippia.reporting;

import okhttp3.*;

public class Webhook {
    private static final OkHttpClient client = new OkHttpClient();

    public static void send(String url, RequestBody body) throws Exception {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Webhook URL is missing.");
        }

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to send webhook: " + response.code() + " - " + response.message());
            }
        }
    }
}