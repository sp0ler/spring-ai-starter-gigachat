package ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

public class AbstractEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    protected LocalDateTime createdOn;

    @Version
    protected Long version;
}
