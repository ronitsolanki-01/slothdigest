package com.slothdigest.digest;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DigestAi {

    private static final String SYSTEM = """
            You are SlothDigest. You read group chat transcripts so busy people don't have to.
            Be concise and factual. Only use information that appears in the transcript.
            Never invent names, dates or tasks. Reply with JSON only.
            """;

    // %1$s = chat name, %2$s = reader's name, %3$s = transcript
    private static final String USER = """
            Chat name: %1$s
            The reader is: %2$s

            Transcript (one message per line, "Sender: text"):
            %3$s

            Fill in these fields:
            - summary: exactly two short sentences describing what happened in the chat.
            - actionItems: concrete tasks, decisions or deadlines that need follow-up
              (include who and when if stated). Use an empty list if there are none.
            - mentions: messages that address or mention %2$s directly. Write each one as a short line
              like "Sam asked if you can bring the projector". Use an empty list if there are none.
            """;

    private final ChatModel chatModel;
    private final String model;
    private final BeanOutputConverter<DigestResult> converter = new BeanOutputConverter<>(DigestResult.class);

    public DigestAi(
            ChatModel chatModel,
            @Value("${spring.ai.ollama.chat.model:qwen3.5:4b}") String model) {
        this.chatModel = chatModel;
        this.model = model;
    }

    public DigestResult summarize(String chatName, String readerName, String transcript) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(this.model)                          // Prevents OllamaChatOptions from defaulting to "mistral"
                .disableThinking()                          // no reasoning trace: faster, cleaner JSON
                .outputSchema(converter.getJsonSchema())    // Ollama enforces this JSON schema
                .build();

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM),
                new UserMessage(USER.formatted(chatName, readerName, transcript))),
                options);

        String json = chatModel.call(prompt).getResult().getOutput().getText();
        return converter.convert(json);
    }
}