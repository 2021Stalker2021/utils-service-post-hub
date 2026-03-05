package com.post_hub.utils_service.mapper;

import com.post_hub.utils_service.entity.ActionLog;
import com.post_hub.utils_service.model.dto.ActionLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ActionLogMapper {

    @Mapping(target = "service", source = "postHubService")
    ActionLogDto map(ActionLog log);

}
