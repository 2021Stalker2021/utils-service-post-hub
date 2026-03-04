package com.post_hub.utils_service.entity;

import com.post_hub.utils_service.model.enums.ActionType;
import com.post_hub.utils_service.model.enums.PostHubService;
import com.post_hub.utils_service.model.enums.PriorityType;
import com.post_hub.utils_service.utils.unum_converter.PostHubServiceConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_logs")
@Getter
@Setter
@ToString
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "action_type", nullable = false, updatable = false)
    /*
     * updatable = false
     * При выполнении session.update(entity) или entityManager.merge(entity) это поле будет проигнорировано
     */
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "priority_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PriorityType priorityType;

    @Column(name = "service", nullable = false, updatable = false)
    @Convert(converter = PostHubServiceConverter.class)
    private PostHubService postHubService;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "is_read", nullable = false, updatable = false)
    private Boolean isRead;
}
