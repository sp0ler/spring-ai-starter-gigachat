package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class ConversationEntity extends AbstractEntity {

    @Column(name = "conversation_id", unique = true, nullable = false)
    private UUID conversationId;

    @OrderBy("createdOn")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "conversation", orphanRemoval = true)
    private List<AssistantMessageEntity> assistantMessages = new ArrayList<>();

    @OrderBy("createdOn")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "conversation", orphanRemoval = true)
    private List<SystemMessageEntity> systemMessages = new ArrayList<>();

    @OrderBy("createdOn")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "conversation", orphanRemoval = true)
    private List<ToolResponseMessageEntity> toolResponseMessages = new ArrayList<>();

    @OrderBy("createdOn")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "conversation", orphanRemoval = true)
    private List<UserMessageEntity> userMessages = new ArrayList<>();
}
