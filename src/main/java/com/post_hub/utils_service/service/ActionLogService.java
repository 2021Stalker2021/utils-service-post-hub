package com.post_hub.utils_service.service;

import com.post_hub.utils_service.model.dto.ActionLogDto;
import com.post_hub.utils_service.model.response.PaginationResponse;
import com.post_hub.utils_service.model.response.UtilsResponse;
import org.springframework.data.domain.Pageable;

public interface ActionLogService {

    UtilsResponse<ActionLogDto> getActionLogById(Integer logId, Integer userId);

    UtilsResponse<PaginationResponse<ActionLogDto>> findAllActionLogs(Pageable pageable);
}
