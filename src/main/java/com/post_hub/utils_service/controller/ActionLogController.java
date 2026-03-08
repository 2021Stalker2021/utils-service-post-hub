package com.post_hub.utils_service.controller;

import com.post_hub.utils_service.model.constant.ApiLogMessage;
import com.post_hub.utils_service.model.dto.ActionLogDto;
import com.post_hub.utils_service.model.response.PaginationResponse;
import com.post_hub.utils_service.model.response.UtilsResponse;
import com.post_hub.utils_service.service.ActionLogService;
import com.post_hub.utils_service.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/actionLogs")
@RequiredArgsConstructor
public class ActionLogController {
    private final ActionLogService service;

    @GetMapping("/{id}")
    public ResponseEntity<UtilsResponse<ActionLogDto>> getActionLog(
            @PathVariable("id") Integer logId,
            @RequestParam(name = "userId", required = false) Integer userId
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        return ResponseEntity.ok(service.getActionLogById(logId, userId));
    }

    @GetMapping("/all")
    public ResponseEntity<UtilsResponse<PaginationResponse<ActionLogDto>>> findAllActionLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        UtilsResponse<PaginationResponse<ActionLogDto>> response = service.findAllActionLogs(pageable);
        return ResponseEntity.ok(response);
    }
}
