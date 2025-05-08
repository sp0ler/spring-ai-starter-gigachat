package ru.deevdenis.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.deevdenis.spring_boot_starter_gigachat.SpringBootStarterGigachatApplication;
import ru.deevdenis.spring_boot_starter_gigachat.option.GigaChatOptions;
import ru.deevdenis.spring_boot_starter_gigachat.tools.DateTimeTools;
import ru.gigachat.api.AuthorizationApi;
import ru.gigachat.api.BusinessApi;
import ru.gigachat.api.ChatApi;
import ru.gigachat.api.FilesApi;
import ru.gigachat.api.ModelsApi;
import ru.gigachat.model.Chat;
import ru.gigachat.model.ChatCompletion;
import ru.gigachat.model.Choices;
import ru.gigachat.model.MessagesRes;
import ru.gigachat.model.MessagesResFunctionCall;
import ru.gigachat.model.Token;
import ru.gigachat.model.Usage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SpringBootStarterGigachatApplication.class)
class GigaChatClientTest {

    @MockitoBean
    protected ChatApi chatApi;

    @MockitoBean
    protected FilesApi filesApi;

    @MockitoBean
    protected ModelsApi modelsApi;

    @MockitoBean
    protected BusinessApi businessApi;

    @MockitoBean
    protected AuthorizationApi authorizationApi;

    @Autowired
    protected ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void init() {
        mockAuth();
    }

    private static final GigaChatOptions OPTIONS = GigaChatOptions.builder()
            .model(Chat.ModelEnum.GIGA_CHAT.getValue())
            .internalToolExecutionEnabled(Boolean.TRUE)
            .build();

    @Test
    void simpleQuestionSuccessTest() {
        String expectedResponse = "И тебе привет!";
        mockSimpleQuestion(expectedResponse);

        ChatClient chatClient = chatClientBuilder.defaultOptions(OPTIONS).build();

        String response = assertDoesNotThrow(() -> chatClient.prompt().user("Привет!").call().content());
        assertEquals(expectedResponse, response);
    }

    @Test
    void getCurrentDateQuestionSuccessTest() {
        String expectedResponse = "Сегодня 13.04.2025";
        mockCurrentDateQuestion(expectedResponse);

        ChatClient chatClient = chatClientBuilder.defaultOptions(OPTIONS).build();

        String response = assertDoesNotThrow(() ->
                chatClient.prompt()
                        .user("Какое сегодня число?")
                        .tools(new DateTimeTools())
                        .call().content()
        );
        assertEquals(expectedResponse, response);
    }

    @Test
    void getTimeQuestionSuccessTest() {
        String expectedResponse = "Сегодня 13.04.2025";
        mockDateWithTimeZoneDateQuestion(expectedResponse);

        ChatClient chatClient = chatClientBuilder.defaultOptions(OPTIONS).build();

        String response = assertDoesNotThrow(() ->
                chatClient.prompt()
                        .user("Какое сегодня число в Новосибирске?")
                        .tools(new DateTimeTools())
                        .call().content()
        );
        assertEquals(expectedResponse, response);
    }

    private void mockAuth() {
        Token token = new Token()
                .accessToken("TEST TOKEN")
                .expiresAt(System.currentTimeMillis() + 1000000000);

        when(authorizationApi.postToken(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(token);
    }

    private void mockSimpleQuestion(String answer) {
        when(chatApi.postChat(anyString(), anyString(), anyString(), anyString(), any(Chat.class)))
                .thenReturn(buildChatCompletion(answer));
    }

    private void mockCurrentDateQuestion(String answer) {
        ChatCompletion chatCompletionFirst = buildChatCompletion("");
        chatCompletionFirst.getChoices().forEach(it -> {
            it.getMessage().setFunctionCall(new MessagesResFunctionCall().name("getCurrentDateTime").arguments(Collections.emptyMap()));
            it.getMessage().setFunctionsStateId(UUID.randomUUID().toString());
            it.setFinishReason(Choices.FinishReasonEnum.FUNCTION_CALL);
        });

        ChatCompletion chatCompletionSecond = buildChatCompletion(answer);

        when(chatApi.postChat(anyString(), anyString(), anyString(), anyString(), any(Chat.class)))
                .thenReturn(chatCompletionFirst)
                .thenReturn(chatCompletionSecond);
    }

    private void mockDateWithTimeZoneDateQuestion(String answer) {
        ChatCompletion chatCompletionFirst = buildChatCompletion("");
        chatCompletionFirst.getChoices().forEach(it -> {
            it.getMessage().setFunctionCall(new MessagesResFunctionCall()
                    .name("getCurrentDateTimeWithZoneId")
                    .arguments(Map.of("zoneId", "Asia/Novosibirsk")));
            it.getMessage().setFunctionsStateId(UUID.randomUUID().toString());
            it.setFinishReason(Choices.FinishReasonEnum.FUNCTION_CALL);
        });

        ChatCompletion chatCompletionSecond = buildChatCompletion(answer);

        when(chatApi.postChat(anyString(), anyString(), anyString(), anyString(), any(Chat.class)))
                .thenReturn(chatCompletionFirst)
                .thenReturn(chatCompletionSecond);
    }

    private ChatCompletion buildChatCompletion(String answer) {
        Choices choices = new Choices()
                .message(new MessagesRes().content(answer).role(MessagesRes.RoleEnum.ASSISTANT))
                .index(0)
                .finishReason(Choices.FinishReasonEnum.STOP);
        return new ChatCompletion()
                .choices(List.of(choices))
                .created((int) System.currentTimeMillis())
                .model("GigaChat:1.0.26.20").
                _object("chat.completion")
                .usage(new Usage().promptTokens(64).completionTokens(35).totalTokens(99).precachedPromptTokens(0));
    }
}
