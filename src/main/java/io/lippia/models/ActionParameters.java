package io.lippia.models;

import java.util.List;

public record ActionParameters(
        // Navigation parameters
        String url,

        // Element interaction parameters
        String element,
        String ref,
        String text,
        String key,
        List<String> values,

        // File upload parameters
        List<String> paths,

        // Dialog parameters
        Boolean accept,
        String promptText,

        // Browser window parameters
        Integer width,
        Integer height,

        // Tab parameters
        Integer index,

        // Screenshot/PDF parameters
        String filename,
        Boolean raw,

        // Wait parameters
        Integer time,
        String textGone,

        // Typing parameters
        Boolean submit,
        Boolean slowly,

        // Screen coordinates parameters
        Integer x,
        Integer y,
        Integer startX,
        Integer startY,
        Integer endX,
        Integer endY,

        // Drag parameters
        String startElement,
        String startRef,
        String endElement,
        String endRef,

        // Test generation parameters
        String name,
        String description,
        List<String> steps
) {
}