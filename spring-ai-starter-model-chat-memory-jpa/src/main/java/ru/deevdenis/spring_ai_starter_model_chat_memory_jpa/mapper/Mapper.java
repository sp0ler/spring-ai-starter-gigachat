package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.mapper;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.AbstractEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.AssistantMessageEntity;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public List<Message> map(List<AbstractEntity> list) {
        List<Message> messages = new ArrayList<>(list.size());

        for (AbstractEntity entity : list) {
            if (entity instanceof AssistantMessageEntity assistant) {

                messages.add(
                        new AssistantMessage(assistant.getMessage())
                );

            }
        }

        return messages;
    }
}
