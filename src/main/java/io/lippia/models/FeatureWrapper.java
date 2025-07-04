package com.crowdar.models;

public record FeatureWrapper(
        Feature feature,
        String action,
        String url,
        String by,
        String value,
        String text
) {
}