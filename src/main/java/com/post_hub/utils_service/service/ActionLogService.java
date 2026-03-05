package com.post_hub.utils_service.service;

import com.post_hub.utils_service.model.dto.ActionLogDto;
import com.post_hub.utils_service.model.response.UtilsResponse;

public interface ActionLogService {

    UtilsResponse<ActionLogDto> getActionLogById(Integer logId, Integer userId);
}
