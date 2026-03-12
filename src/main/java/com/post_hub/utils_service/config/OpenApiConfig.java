package com.post_hub.utils_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "'POST_HUB' REST API", // Заголовок в Swagger UI
                version = "1.0",    // Версия API
                description = "'UTILS-service' REST API",   // Описание
                contact = @Contact(name = "POST_HUB")),     // Контакт разработчика
        security = { @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)}
        //            ↑ говорит: "все эндпоинты по умолчанию требуют авторизацию"
        //              HttpHeaders.AUTHORIZATION = "Authorization"
)
@SecurityScheme(
        name = HttpHeaders.AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Value("${swagger.servers.first:#{null}}") // Читает из application.yml
    private String firstServer; // Если нет — будет null

    @Value("${swagger.servers.second:#{null}}") // Читает из application.yml
    private String secondServer; // Если нет — будет null

    @Bean
    public GroupedOpenApi utilsApi() {
        return GroupedOpenApi.builder()
                .group("utils-service") // Имя группы в выпадающем списке
                .packagesToScan("com.post_hub.utils_service.controller") // Какие контроллеры включить
                .addOpenApiCustomizer(serverCustomizer()) // Кастомизация серверов
                .build();
    }

    @Bean
    public OpenApiCustomizer serverCustomizer() {
        return openApi -> {                     // Лямбда-кастомизатор
            List<Server> servers = new ArrayList<>();
            if (Objects.nonNull(firstServer)) {
                servers.add(new Server().url(firstServer).description("API Server"));
            }
            if (Objects.nonNull(secondServer)) {
                servers.add(new Server().url(secondServer).description("Second API Server"));
            }
            openApi.setServers(servers); // Заменяет список серверов
        };
    }
}
