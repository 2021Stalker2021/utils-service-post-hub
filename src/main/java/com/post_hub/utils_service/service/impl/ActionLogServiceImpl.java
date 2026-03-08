package com.post_hub.utils_service.service.impl;

import com.post_hub.utils_service.entity.ActionLog;
import com.post_hub.utils_service.mapper.ActionLogMapper;
import com.post_hub.utils_service.model.constant.ApiErrorMessage;
import com.post_hub.utils_service.model.dto.ActionLogDto;
import com.post_hub.utils_service.model.exception.NotFoundException;
import com.post_hub.utils_service.model.request.ActionLogIsReadRequest;
import com.post_hub.utils_service.model.request.ActionLogUpdateResultDTO;
import com.post_hub.utils_service.model.response.PaginationResponse;
import com.post_hub.utils_service.model.response.UtilsResponse;
import com.post_hub.utils_service.repository.ActionLogRepository;
import com.post_hub.utils_service.service.ActionLogService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionLogServiceImpl implements ActionLogService {
    private final ActionLogRepository repository;
    private final ActionLogMapper mapper;

    @Override
    public UtilsResponse<ActionLogDto> getActionLogById(Integer logId, Integer userId) {
        ActionLog actionLog;

        if (userId == null) {
            actionLog = repository.findById(logId)
                    .orElseThrow(() -> new NotFoundException(ApiErrorMessage.NOT_FOUND_ACTION_LOG.getMessage(logId)));
        } else {
            actionLog = repository.findByIdAndUserId(logId, userId)
                    .orElseThrow(() -> new NotFoundException(ApiErrorMessage.NOT_FOUND_ACTION_LOG_FOR_USER.getMessage(logId, userId)));
        }

        return UtilsResponse.createSuccessful(mapper.map(actionLog));
    }

    @Override
    public UtilsResponse<PaginationResponse<ActionLogDto>> findAllActionLogs(Pageable pageable) {
        Page<ActionLogDto> logs = repository.findAll(pageable)
                .map(mapper::map);

        PaginationResponse<ActionLogDto> paginationResponse = new PaginationResponse<>(
                logs.getContent(),
                new PaginationResponse.Pagination(
                        logs.getTotalElements(),
                        pageable.getPageSize(),
                        logs.getNumber() +1,
                        logs.getTotalPages()
                )
        );

        return UtilsResponse.createSuccessful(paginationResponse);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    // 1. Объединение нескольких операций с БД в одну транзакцию
    // 2. Обеспечение ACID
    // 3. Указывает, что транзакция должна откатываться при любом RuntimeException
    public UtilsResponse<ActionLogUpdateResultDTO> setIsReadEqualsTrue(@NotNull ActionLogIsReadRequest request) {
        Integer currentUserId = request.getUserId(); // id пользователя, который отправил запрос

        List<ActionLog> logs = repository.findAllById(request.getIds()); // логи которые нужно отметить как прочитанные

        Map<Boolean, List<Integer>> partitioned = logs.stream()
                .collect(Collectors.partitioningBy(
                        log -> log.getUserId().equals(currentUserId),
                        Collectors.mapping(ActionLog::getId, Collectors.toList())
                ));

        List<Integer> allowedIds = partitioned.get(true);
        List<Integer> skippedIds = partitioned.get(false);

        int updatedCount = allowedIds.isEmpty() ? 0 : repository.setIsReadEqualsTrue(allowedIds);

        return UtilsResponse.createSuccessful(
                ActionLogUpdateResultDTO.builder()
                        .updatedCount(updatedCount)
                        .updatedIds(allowedIds)
                        .skippedIds(skippedIds)
                        .build()
        );
    }
}
