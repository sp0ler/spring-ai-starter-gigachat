package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.ToolResponseMessageEntity;

import java.util.UUID;

public interface ToolResponseMessageRepository extends JpaRepository<ToolResponseMessageEntity, UUID> {
}
