package com.post_hub.utils_service.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ActionLogUpdateResultDTO {
    private int updatedCount; // сколько логов удалось обновить
    private List<Integer> updatedIds; // список логов которые были успешно обновлены
    private List<Integer> skippedIds; // логи которые мы пропустили (потому что они не принадлежат этому пользователю)
}
