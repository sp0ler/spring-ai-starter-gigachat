package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.mapper;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ConversationEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.MessageEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageMapper {

    public MessageEntity fromDto(Message message, @Nullable ConversationEntity conversation) {
        MessageEntity messageEntity = new MessageEntity();

        if (message == null) {
            return messageEntity;
        }

        messageEntity.setMessageType(message.getMessageType());
        messageEntity.setContent(message.getText());

        if (conversation != null) {
            messageEntity.setConversation(conversation);
        }

        return messageEntity;
    }

    public Message toDto(MessageEntity messageEntity) {
        return switch (messageEntity.getMessageType()) {
            case USER -> new UserMessage(messageEntity.getContent());
            case ASSISTANT -> new AssistantMessage(messageEntity.getContent());
            case TOOL -> new ToolResponseMessage(List.of());
            case SYSTEM -> new SystemMessage(messageEntity.getContent());
        };
    }

    public List<MessageEntity> fromDtoList(List<Message> messages, @Nullable ConversationEntity conversation) {
        List<MessageEntity> list = new ArrayList<>(messages.size());
        for (Message message : messages) {
            list.add(fromDto(message, conversation));
        }

        return list;
    }

    public List<Message> toDtoList(List<MessageEntity> messages) {
        List<Message> list = new ArrayList<>(messages.size());
        for (MessageEntity messageEntity : messages) {
            list.add(toDto(messageEntity));
        }

        return list;
    }
}
