package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ConversationEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.MessageEntity;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.mapper.MessageMapper;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository.ConversationRepository;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository.MessageRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMemoryService implements ChatMemory {

    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    @Transactional
    public void add(String conversationId, Message message) {
        checkConversationId(conversationId);
        ConversationEntity conversation = getOrCreateConversation(conversationId);

        messageRepository.save(
                messageMapper.fromDto(message, conversation)
        );
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        checkConversationId(conversationId);
        ConversationEntity conversation = getOrCreateConversation(conversationId);

        messageRepository.saveAll(
                messageMapper.fromDtoList(messages, conversation)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> get(String conversationId, int lastN) {
        checkConversationId(conversationId);
        Pageable pageable = PageRequest.of(0, lastN, Sort.by("createdOn").descending());
        List<MessageEntity> messages = messageRepository.findByConversationIdOrderByCreatedOn(
                UUID.fromString(conversationId), pageable
        );
        return messageMapper.toDtoList(messages);
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        checkConversationId(conversationId);
        conversationRepository.deleteByConversationId(UUID.fromString(conversationId));
    }

    private ConversationEntity getOrCreateConversation(String conversationId) {
        ConversationEntity conversation = conversationRepository.findByConversationId(UUID.fromString(conversationId));
        if (conversation == null) {
            conversation = new ConversationEntity();
            conversation.setConversationId(UUID.fromString(conversationId));
        }

        return conversation;
    }

    private void checkConversationId(String conversationId) {
        if (!conversationId.matches("[0-9a-f-]+")) {
            throw new IllegalArgumentException("Invalid conversationId");
        }
    }
}
