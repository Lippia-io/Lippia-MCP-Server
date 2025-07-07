package io.lippia.models;

import java.util.List;

public record Step(String description, List<Action> actions) {
}