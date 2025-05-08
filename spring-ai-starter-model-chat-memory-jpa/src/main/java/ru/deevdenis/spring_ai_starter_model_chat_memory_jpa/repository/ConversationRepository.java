package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ConversationEntity;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @EntityGraph(attributePaths = {"assistantMessages", "systemMessages", "toolResponseMessages", "userMessages"})
    ConversationEntity findByConversationId(UUID conversationId);

    @Query("from ConversationEntity.conversationId")
    List<UUID> findConversationIds();
}
