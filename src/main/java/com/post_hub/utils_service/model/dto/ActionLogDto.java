package com.post_hub.utils_service.model.dto;

import com.post_hub.utils_service.model.enums.ActionType;
import com.post_hub.utils_service.model.enums.PostHubService;
import com.post_hub.utils_service.model.enums.PriorityType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActionLogDto {
    private Integer id;
    private Integer userId;

    private ActionType actionType;
    private PriorityType priorityType;
    private PostHubService service;

    private LocalDateTime createdAt;
    private String message;
    private Boolean isRead;
}
