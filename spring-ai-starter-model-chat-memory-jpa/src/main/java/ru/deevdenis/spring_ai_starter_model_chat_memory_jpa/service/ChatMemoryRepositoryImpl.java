package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.AbstractEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ConversationEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository.ConversationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMemoryRepositoryImpl implements ChatMemoryRepository {

    private final ConversationRepository conversationRepository;

    @Override
    public List<String> findConversationIds() {
        return conversationRepository.findConversationIds().stream().map(UUID::toString).toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        ConversationEntity conversation = conversationRepository.findByConversationId(UUID.fromString(conversationId));

        List<AbstractEntity> messages = new ArrayList<>();
        messages.addAll(conversation.getUserMessages());
        messages.addAll(conversation.getAssistantMessages());
        messages.addAll(conversation.getToolResponseMessages());
        messages.addAll(conversation.getSystemMessages());

        return List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}
