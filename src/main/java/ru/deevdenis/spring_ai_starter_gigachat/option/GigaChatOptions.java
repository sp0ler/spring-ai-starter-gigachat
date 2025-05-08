package ru.deevdenis.spring_ai_starter_gigachat.option;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GigaChatOptions implements ToolCallingChatOptions {

    @JsonIgnore
    private Boolean internalToolExecutionEnabled;

    @JsonIgnore
    @lombok.Builder.Default
    private Map<String, Object> toolContext = new HashMap<>();

    @JsonIgnore
    @lombok.Builder.Default
    private Set<String> toolNames = new HashSet<>();

    @JsonIgnore
    @lombok.Builder.Default
    private List<FunctionCallback> toolCallbacks = new ArrayList<>();

    @JsonIgnore
    @lombok.Builder.Default
    private List<FunctionCallback> functionCallbacks = new ArrayList<>();

    @JsonIgnore
    @lombok.Builder.Default
    private Map<String, Object> context = new HashMap<>();

    @JsonIgnore
    @lombok.Builder.Default
    private Set<String> functions = new HashSet<>();

    @JsonIgnore
    @lombok.Builder.Default
    private List<String> stopSequences = new ArrayList<>();

    private String function;

    private String model;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @lombok.Builder.Default
    @JsonProperty("max_tokens")
    private Integer maxTokens = 1000;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @lombok.Builder.Default
    @JsonProperty("temperature")
    private Double temperature = 1.0;

    @JsonProperty("top_k")
    private Integer topK;

    @lombok.Builder.Default
    @JsonProperty("top_p")
    private Double topP = 0.7;

    @JsonProperty("user")
    private String user;

    @JsonProperty("n")
    private Integer n;

    @JsonProperty("stop")
    private List<String> stop;

    @Override
    public GigaChatOptions copy() {
        return GigaChatOptions.builder()
                .model(this.getModel())
                .n(this.getN())
                .topP(this.getTopP())
                .stop(this.getStop())
                .function(this.getFunction())
                .context(this.getContext())
                .functions(this.getFunctions())
                .stopSequences(this.getStopSequences())
                .functionCallbacks(this.getFunctionCallbacks())
                .toolContext(this.getToolContext())
                .toolNames(this.getToolNames())
                .toolCallbacks(this.getToolCallbacks())
                .maxTokens(this.getMaxTokens())
                .temperature(this.getTemperature())
                .topK(this.getTopK())
                .presencePenalty(this.getPresencePenalty())
                .frequencyPenalty(this.getFrequencyPenalty())
                .user(this.getUser())
                .build();
    }

    @Override
    @JsonIgnore
    public List<FunctionCallback> getToolCallbacks() {
        return this.toolCallbacks;
    }

    @Override
    @JsonIgnore
    public void setToolCallbacks(List<FunctionCallback> toolCallbacks) {
        Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
        Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    @JsonIgnore
    public Set<String> getToolNames() {
        return this.toolNames;
    }

    @Override
    @JsonIgnore
    public void setToolNames(Set<String> toolNames) {
        Assert.notNull(toolNames, "toolNames cannot be null");
        Assert.noNullElements(toolNames, "toolNames cannot contain null elements");
        this.toolNames = toolNames;
    }

    @JsonIgnore
    public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
        this.functionCallbacks = functionCallbacks;

        this.setToolCallbacks(functionCallbacks);
        this.setToolNames(
                functionCallbacks.stream().map(FunctionCallback::getName).collect(Collectors.toSet())
        );
    }

    @Override
    @Nullable
    @JsonIgnore
    public Boolean isInternalToolExecutionEnabled() {
        return internalToolExecutionEnabled;
    }

    @Override
    @JsonIgnore
    public void setInternalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled) {
        this.internalToolExecutionEnabled = internalToolExecutionEnabled;
    }
}
