package ru.deevdenis.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.controller.api.DefaultApi;
import ru.controller.model.ChatRequest;
import ru.controller.model.ChatResponse;
import ru.controller.model.UploadChatResponse;
import ru.deevdenis.spring_ai_starter_gigachat.model.GigaChatModel;
import ru.deevdenis.spring_ai_starter_gigachat.option.GigaChatOptions;
import ru.deevdenis.spring_ai_starter_gigachat.tools.DateTimeTools;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.service.ChatMemoryService;

import java.util.UUID;

@RestController
public class ExampleController implements DefaultApi {

    private final ChatClient chatClient;
    private final ChatMemoryService chatMemoryRepository;

    public ExampleController(
            GigaChatModel gigaChatModel,
            ChatMemoryService chatMemoryRepository
    ) {
        this.chatClient = ChatClient.create(gigaChatModel);
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Override
    public ResponseEntity<ChatResponse> chatPost(ChatRequest chatRequest) {
        UUID conversationId = chatRequest.getConversationId() != null ? chatRequest.getConversationId() : UUID.randomUUID();

        String content = chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemoryRepository).conversationId(conversationId.toString()).build())
                .options(GigaChatOptions.builder().model(chatRequest.getModel()).internalToolExecutionEnabled(Boolean.TRUE).build())
                .tools(new DateTimeTools())
                .user(chatRequest.getMessage())
                .call()
                .content();

        return ResponseEntity.ok(new ChatResponse(content));
    }

    @Override
    public ResponseEntity<UploadChatResponse> chatFilePost(MultipartFile file, String filename, String userMessage, String model, String mimeType, String systemMessage) {
        String content = chatClient.prompt()
                .options(GigaChatOptions.builder().model(model).internalToolExecutionEnabled(Boolean.TRUE).build())
                .user(u -> u.media(MimeType.valueOf(mimeType), file.getResource()).text(userMessage))
                .call()
                .content();

        return ResponseEntity.ok(new UploadChatResponse().message(content));
    }
}
