package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "conversation")
public class ConversationEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversation_id", unique = true, nullable = false)
    private UUID conversationId;

    @OrderBy("createdOn")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "conversation", orphanRemoval = true)
    private List<MessageEntity> messages = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    protected LocalDateTime createdOn;

    @Version
    protected Long version;

    @PrePersist
    void prePersist() {
        createdOn = LocalDateTime.now();
    }
}
