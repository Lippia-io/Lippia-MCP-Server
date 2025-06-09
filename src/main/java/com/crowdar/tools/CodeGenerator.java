package com.crowdar.tools;

import com.crowdar.models.Flow;
import com.crowdar.utils.PromptBuilder;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.Optional;

public class CodeGenerator {

    public static Optional<String> fromFlow(Flow flow) {
        String prompt = PromptBuilder.buildPromptFromActionsForWeb(flow);

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .fromEnv()
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.CHATGPT_4O_LATEST)
                .addUserMessage(prompt)
                .build();

        try {
            ChatCompletion chatCompletion = client.chat().completions().create(params);

            if (chatCompletion.choices().isEmpty()) return Optional.empty();

            return chatCompletion.choices().get(0).message().content();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
