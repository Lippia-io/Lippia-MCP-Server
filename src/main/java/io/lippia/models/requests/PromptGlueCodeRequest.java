package com.crowdar.models.requests;

import com.crowdar.models.Action;

import java.util.List;

public record PromptGlueCodeRequest(String projectType, List<Action> actions) {
}
