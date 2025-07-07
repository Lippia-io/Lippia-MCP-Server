package io.lippia.models;

import java.util.List;

public record Scenario(String description, List<Step> steps) {
}