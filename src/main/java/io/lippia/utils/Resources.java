package io.lippia.utils;

import io.lippia.McpServerApplication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Resources {
    public static String load(final String path) {
        try (InputStream input = McpServerApplication.class.getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }

            throw new IOException("Resource not found: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load JsonSchema: " + path, e);
        }
    }
}
