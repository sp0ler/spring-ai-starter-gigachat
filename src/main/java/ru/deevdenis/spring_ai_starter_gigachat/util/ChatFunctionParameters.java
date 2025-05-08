package ru.deevdenis.spring_ai_starter_gigachat.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class ChatFunctionParameters {

    /**
     * Тип параметров функции.
     */
    @JsonProperty
    private String type;

    /**
     * Описание параметров функции.
     */
    @JsonProperty
    private Map<String, ChatFunctionParametersProperty> properties = new HashMap<>();

    /**
     * Список обязательных параметров.
     */
    @JsonProperty
    private Collection<String> required;

    public void addProperty(String key, ChatFunctionParametersProperty value) {
        properties.put(key, value);
    }

    public Set<String> getRequired() {
        return properties.keySet();
    }
}
