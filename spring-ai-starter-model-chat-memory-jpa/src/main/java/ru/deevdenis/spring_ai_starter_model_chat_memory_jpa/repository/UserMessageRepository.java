package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.UserMessageEntity;

import java.util.UUID;

public interface UserMessageRepository extends JpaRepository<UserMessageEntity, UUID> {
}
