package io.lippia.models.requests;

import io.lippia.models.Action;

import java.util.List;

public record PromptGlueCodeRequest(String projectType, List<Action> actions) {
}
