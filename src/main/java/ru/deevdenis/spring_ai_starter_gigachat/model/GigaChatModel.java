package ru.deevdenis.spring_ai_starter_gigachat.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.AbstractToolCallSupport;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Example;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Item;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Parameters;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Property;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.ReturnParameters;
import ru.deevdenis.spring_ai_starter_gigachat.api.GigaChatServiceImpl;
import ru.deevdenis.spring_ai_starter_gigachat.option.GigaChatOptions;
import ru.deevdenis.spring_ai_starter_gigachat.util.ChatFunctionParameters;
import ru.deevdenis.spring_ai_starter_gigachat.util.ChatFunctionParametersProperty;
import ru.deevdenis.spring_ai_starter_gigachat.util.ToolUtils;
import ru.gigachat.model.Chat;
import ru.gigachat.model.ChatCompletion;
import ru.gigachat.model.ChatFunctionsInner;
import ru.gigachat.model.ChatFunctionsInnerFewShotExamplesInner;
import ru.gigachat.model.Choices;
import ru.gigachat.model.FileObject;
import ru.gigachat.model.MessagesRes;
import ru.gigachat.model.MessagesResFunctionCall;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static ru.gigachat.model.Message.RoleEnum.*;

@Slf4j
public class GigaChatModel extends AbstractToolCallSupport implements ChatModel {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();
    private static final String PROVIDER_GIGACHAT = "gigachat";
    private static final GigaChatOptions DEFAULT_OPTIONS =  GigaChatOptions.builder()
            .temperature(1.0)
            .topP(0.7)
            .maxTokens(1000)
            .build();

    private final GigaChatServiceImpl gigaChatService;
    private final ObservationRegistry observationRegistry;
    private ChatModelObservationConvention observationConvention;
    private final ToolCallingManager toolCallingManager;

    public GigaChatModel(
            ObservationRegistry observationRegistry,
            GigaChatServiceImpl gigaChatService,
            ToolCallingManager toolCallingManager
    ) {
        super(null, DEFAULT_OPTIONS, Collections.emptyList());
        Assert.notNull(observationRegistry, "observationRegistry must not be null");

        this.gigaChatService = gigaChatService;
        this.observationRegistry = observationRegistry;
        this.toolCallingManager = toolCallingManager;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Prompt requestPrompt = buildRequestPrompt(prompt);
        return internalCall(requestPrompt, null);
    }


    public ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider(PROVIDER_GIGACHAT)
                .requestOptions(prompt.getOptions() != null ? prompt.getOptions() : DEFAULT_OPTIONS) // bug, constructor requires requestOptions
                .build();

        ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, this.observationRegistry)
                .observe(() -> {
                    GigaChatOptions options = ModelOptionsUtils.copyToTarget(
                            (ToolCallingChatOptions) prompt.getOptions(), ToolCallingChatOptions.class, GigaChatOptions.class);


                    String requestId = UUID.randomUUID().toString();
                    Chat gigaChatRequest = toGigaChatRequest(prompt, options);
                    ChatCompletion chatCompletion = gigaChatService.sendMessage(gigaChatRequest, requestId);

                    ChatResponse chatResponse = toChatResponse(chatCompletion, previousChatResponse, requestId);
                    observationContext.setResponse(chatResponse);

                    return chatResponse;
                });

        boolean isToolCalls = !Optional.ofNullable(response)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AssistantMessage::getToolCalls)
                .orElse(Collections.emptyList())
                .isEmpty();

        if (ToolUtils.isToolsEnabled(prompt.getOptions()) && isToolCalls) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, response);
            if (toolExecutionResult.returnDirect()) {
                // Return tool execution result directly to the client.
                return ChatResponse.builder()
                        .from(response)
                        .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                        .build();
            }
            else {
                // Send the tool execution result back to the model.
                return internalCall(
                        new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()), response
                );
            }
        }

        return response;
    }

    private Chat toGigaChatRequest(Prompt prompt, GigaChatOptions options) {
        List<ChatFunctionsInner> functions = new ArrayList<>(options.getFunctionCallbacks().size());

        for (FunctionCallback functionCallback : options.getFunctionCallbacks()) {
            if (functionCallback instanceof MethodToolCallback methodToolCallback) {
                try {
                    Field toolMethod = methodToolCallback.getClass().getDeclaredField("toolMethod");
                    toolMethod.setAccessible(true);
                    Method method = (Method) toolMethod.get(methodToolCallback);

                    Example example = method.getAnnotation(Example.class);
                    Parameters parameters = method.getAnnotation(Parameters.class);
                    ReturnParameters returnParameters = method.getAnnotation(ReturnParameters.class);

                    Assert.notNull(example, "Example annotation is required for method: %s".formatted(method.getName()));
                    Assert.notNull(parameters, "Parameters annotation is required for method: %s".formatted(method.getName()));
                    Assert.notNull(returnParameters, "Schema annotation is required for method: %s".formatted(method.getName()));

                    ChatFunctionParameters chatFunctionParameters = toChatFunctionParameters(
                            parameters.type(), parameters.properties()
                    );
                    ChatFunctionParameters chatFunctionReturnParameters = toChatFunctionParameters(
                            returnParameters.type(), returnParameters.properties()
                    );

                    ChatFunctionsInner func = new ChatFunctionsInner()
                            .name(functionCallback.getName())
                            .description(functionCallback.getDescription())
                            .parameters(chatFunctionParameters);

                    for (String exampleValue : example.value()) {
                        Map<String, Object> exampleParams = new LinkedHashMap<>();
                        for (Item item : example.items()) {
                            exampleParams.put(item.key(), item.value());
                        }

                        func.addFewShotExamplesItem(new ChatFunctionsInnerFewShotExamplesInner()
                                .request(exampleValue)
                                .params(exampleParams))
                                .returnParameters(chatFunctionReturnParameters);
                    }

                    functions.add(func);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    log.debug("Failed to get toolMethod", e);
                }
            }
        }

        List<ru.gigachat.model.Message> messages = nullSafeList(prompt.getInstructions()).stream()
                .map(this::mapMessage)
                .toList();

        return new Chat()
                .model(Chat.ModelEnum.fromValue(options.getModel()))
                .functions(functions)
                .maxTokens(options.getMaxTokens())
                .temperature(options.getTemperature())
                .topP(options.getTopP())
                .messages(messages);
    }

    protected Prompt buildRequestPrompt(Prompt prompt) {
        // Process runtime options
        GigaChatOptions runtimeOptions;
        if (prompt.getOptions() == null) {
            runtimeOptions = DEFAULT_OPTIONS;
        } else {
            runtimeOptions = ModelOptionsUtils.copyToTarget(
                    (ToolCallingChatOptions) prompt.getOptions(), ToolCallingChatOptions.class, GigaChatOptions.class);
        }

        ToolCallingChatOptions.validateToolCallbacks(runtimeOptions.getToolCallbacks());

        return new Prompt(prompt.getInstructions(), runtimeOptions);
    }

    public GigaChatOptions getDefaultOptions() {
        return DEFAULT_OPTIONS;
    }

    private ru.gigachat.model.Message mapMessage(Message message) {
        switch (message.getMessageType()) {
            case USER -> {
                UserMessage userMessage = (UserMessage) message;

                List<String> attachments = new ArrayList<>(userMessage.getMedia().size());
                for (Media media : userMessage.getMedia()) {
                    byte[] dataAsByteArray = media.getDataAsByteArray();
                    MimeType mimeType = media.getMimeType();
                    String filename = media.getName();

                    FileObject fileObject = gigaChatService.uploadFile(
                            dataAsByteArray, UUID.randomUUID().toString(), "general", filename, mimeType.toString());

                    attachments.add(fileObject.getId());
                }

                return new ru.gigachat.model.Message()
                        .content(userMessage.getText())
                        .attachments(attachments)
                        .role(USER);
            }
            case SYSTEM -> {
                return new ru.gigachat.model.Message()
                        .content(message.getText())
                        .role(SYSTEM);
            }
            case ASSISTANT -> {
                AssistantMessage assistantMessage = (AssistantMessage) message;
                return new ru.gigachat.model.Message()
                        .functionsStateId((String) assistantMessage.getMetadata().getOrDefault("functionsStateId", StringUtils.EMPTY))
                        .content(assistantMessage.getText())
                        .role(ASSISTANT);
            }
            case TOOL -> {
                ToolResponseMessage toolMessage = (ToolResponseMessage) message;
                String responseData = toolMessage.getResponses().stream()
                        .map(ToolResponseMessage.ToolResponse::responseData)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No responses found for tool"));

                return new ru.gigachat.model.Message()
                        .functionsStateId((String) toolMessage.getMetadata().getOrDefault("functionsStateId", StringUtils.EMPTY))
                        .content(responseData)
                        .role(ru.gigachat.model.Message.RoleEnum.FUNCTION);
            }
        }

        throw new IllegalArgumentException("Unsupported message type: %s".formatted(message.getMessageType()));
    }

    private ChatResponse toChatResponse(ChatCompletion chatCompletion, ChatResponse previousChatResponse, String requestId) {
        List<Generation> generations = nullSafeList(chatCompletion.getChoices()).stream().map(choice -> {
            Optional<Choices> choicesOptional = Optional.ofNullable(choice);

            String role = choicesOptional.map(Choices::getMessage).map(MessagesRes::getRole).map(MessagesRes.RoleEnum::getValue).orElse(StringUtils.EMPTY);
            int choiceIndex = choicesOptional.map(Choices::getIndex).orElse(0);
            String finishResult = choicesOptional.map(Choices::getFinishReason).map(Choices.FinishReasonEnum::getValue).orElse(StringUtils.EMPTY);
            String functionsStateId = choicesOptional.map(Choices::getMessage).map(MessagesRes::getFunctionsStateId).orElse(StringUtils.EMPTY);

            Map<String, Object> metadata = Map.of(
                    "id", requestId,
                    "role", role,
                    "choiceIndex", choiceIndex,
                    "finishReason", finishResult,
                    "functionsStateId", functionsStateId
            );

            return buildGeneration(choicesOptional, metadata);
        }).toList();

        ru.gigachat.model.Usage currentUsage = chatCompletion.getUsage();
        Usage cumulativeUsage = getCumulativeUsage(currentUsage, previousChatResponse);

        return new ChatResponse(generations, from(chatCompletion, cumulativeUsage, requestId));
    }

    private Generation buildGeneration(Optional<Choices> choice, Map<String, Object> metadata) {
        Optional<MessagesResFunctionCall> functionCall = choice.map(Choices::getMessage).map(MessagesRes::getFunctionCall);
        String name = functionCall.map(MessagesResFunctionCall::getName).orElse("");
        String id = (String) metadata.get("id");

        String arguments = functionCall
                .map(MessagesResFunctionCall::getArguments)
                .map(it -> {
                    try {
                        return OBJECT_MAPPER.writeValueAsString(it);
                    } catch (JsonProcessingException e) {
                        return StringUtils.EMPTY;
                    }
                }).orElse(StringUtils.EMPTY);

        ChatGenerationMetadata generationMetadata = ChatGenerationMetadata.builder()
                .finishReason(choice.map(Choices::getFinishReason).map(Enum::name).orElse(""))
                .metadata(metadata)
                .build();

        List<AssistantMessage.ToolCall> toolCalls;
        if (name.isEmpty()) {
            toolCalls = Collections.emptyList();
        } else {
            toolCalls = List.of(
                    new AssistantMessage.ToolCall(id, "function", name, arguments)
            );
        }


        AssistantMessage assistantMessage = new AssistantMessage(
                choice.map(Choices::getMessage).map(MessagesRes::getContent).orElse(""), metadata, toolCalls
        );

        return new Generation(assistantMessage, generationMetadata);
    }

    private Usage getCumulativeUsage(ru.gigachat.model.Usage currentUsage, ChatResponse previousChatResponse) {
        Usage usageFromPreviousChatResponse = null;
        if (previousChatResponse != null && previousChatResponse.getMetadata() != null
                && previousChatResponse.getMetadata().getUsage() != null) {
            usageFromPreviousChatResponse = previousChatResponse.getMetadata().getUsage();
        }
        else {
            // Return the curent usage when the previous chat response usage is empty or
            // null.
            return new DefaultUsage(currentUsage.getPromptTokens(), currentUsage.getCompletionTokens(), currentUsage.getTotalTokens());
        }
        // For a valid usage from previous chat response, accumulate it to the current
        // usage.
        if (!isEmpty(currentUsage)) {
            Integer promptTokens = currentUsage.getPromptTokens();
            Integer generationTokens = currentUsage.getCompletionTokens();
            Integer totalTokens = currentUsage.getTotalTokens();
            // Make sure to accumulate the usage from the previous chat response.
            promptTokens += usageFromPreviousChatResponse.getPromptTokens();
            generationTokens += usageFromPreviousChatResponse.getCompletionTokens();
            totalTokens += usageFromPreviousChatResponse.getTotalTokens();
            return new DefaultUsage(promptTokens, generationTokens, totalTokens);
        }
        // When current usage is empty, return the usage from the previous chat response.
        return usageFromPreviousChatResponse;
    }

    public ChatResponseMetadata from(ChatCompletion chatCompletion, Usage usage, String requestId) {
        return ChatResponseMetadata.builder()
                .model(chatCompletion.getModel())
                .id(requestId)
                .usage(usage)
                .metadata(Map.of("created", chatCompletion.getCreated(), "object", chatCompletion.getObject()))
                .build();
    }

    private ChatFunctionParameters toChatFunctionParameters(String type, Property[] properties) {
        ChatFunctionParameters chatFunctionParameters = new ChatFunctionParameters();

        if (StringUtils.equals(type, "void")) {
            return chatFunctionParameters;
        }

        chatFunctionParameters.setType(type);

        for (Property property : properties) {
            if (StringUtils.isEmpty(property.type())) {
                continue;
            }

            ChatFunctionParametersProperty chatFunctionParametersProperty = new ChatFunctionParametersProperty();
            chatFunctionParametersProperty.setType(property.type());
            chatFunctionParametersProperty.setDescription(property.description());
            chatFunctionParametersProperty.setEnums(Arrays.asList(property.enums()));

            for (Item item : property.items()) {
                chatFunctionParametersProperty.addItems(item.key(), item.value());
            }

            chatFunctionParameters.addProperty(property.name(), chatFunctionParametersProperty);
        }

        return chatFunctionParameters;
    }

    private <T> List<T> nullSafeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

}
