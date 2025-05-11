package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ConversationEntity;

import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"messages"})
    ConversationEntity findByConversationId(UUID conversationId);

    void deleteByConversationId(UUID conversationId);
}
