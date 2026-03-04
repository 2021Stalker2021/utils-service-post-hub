package com.post_hub.utils_service.utils.unum_converter;

import com.post_hub.utils_service.model.enums.PostHubService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PostHubServiceConverter implements AttributeConverter<PostHubService, String> {
    @Override
    public String convertToDatabaseColumn(PostHubService attribute) {
        return attribute.getValue();
    }

    @Override
    public PostHubService convertToEntityAttribute(String dbData) {
        return PostHubService.fromValue(dbData);
    }
}
