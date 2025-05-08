package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity.SystemMessageEntity;

import java.util.UUID;

public interface SystemMessageRepository extends JpaRepository<SystemMessageEntity, UUID> {
}
