package com.post_hub.utils_service.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ActionLogIsReadRequest {
    private Integer userId; // пользователь, который отправил запрос
    private List<Integer> ids; // список логов, которые нужно отметить как прочитанные
}
