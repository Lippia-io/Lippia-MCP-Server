package com.crowdar.models.requests;

import com.crowdar.models.Action;

import java.util.List;

public record PromptRequest(String projectType, List<Action> actions) {
}
