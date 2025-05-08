package ru.deevdenis.spring_ai_starter_gigachat.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChatFunctionParametersProperty {

    /**
     * Тип аргумента функции
     */
    @JsonProperty
    private String type;

    /**
     * Описание аргумента
     */
    @JsonProperty
    private String description;

    /**
     * Возможные значения аргумента
     */
    @JsonProperty
    private Map<String, Object> items = new HashMap<>();

    /**
     * Возможные значения enum
     */
    @JsonProperty("enum")
    private List<String> enums = new ArrayList<>();

    /**
     * Описание параметров аргумента.
     */
    @JsonProperty
    private Map<String, ChatFunctionParametersProperty> properties = new HashMap<>();

    public void addProperties(String key, ChatFunctionParametersProperty value) {
        properties.put(key, value);
    }

    public void addItems(String key, Object value) {
        items.put(key, value);
    }

    public void addEnum(String enumName) {
        enums.add(enumName);
    }
}
