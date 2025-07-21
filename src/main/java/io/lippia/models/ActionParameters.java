package io.lippia.models;

public record ActionParameters(
        // Navigation parameters
        String url,

        // Element locator parameters (specific to Lippia)
        String by,
        String selector,
        
        // Text interaction parameters
        String text,
        
        // Screenshot parameters
        String filename,
        
        // Dummy parameter for actions that don't require parameters
        String random_string
) {
}