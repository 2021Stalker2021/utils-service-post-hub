package com.post_hub.utils_service.security.filter;


import com.post_hub.utils_service.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * JwtTokenFilter - это фильтр, который перехватывает каждый HTTP запрос к сервису
 * и проверяет наличие и валидность JWT токена в заголовке Authorization.
 *
 * OncePerRequestFilter гарантирует, что фильтр выполнится только один раз за запрос
 * (даже если запрос проходит через несколько цепочек фильтров).
 */
@Slf4j  // Lombok - создает поле log для логирования
@Component  // Spring создаст бин этого класса
@RequiredArgsConstructor  // Lombok - создает конструктор с final полями (jwtTokenProvider)
public class JwtTokenFilter extends OncePerRequestFilter {

    /** Название заголовка, в котором передается токен */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Префикс токена в заголовке (с пробелом после Bearer) */
    private static final String BEARER_PREFIX = "Bearer ";

    /** Сервис для работы с JWT токенами (валидация, извлечение данных) */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Главный метод фильтра, который вызывается для каждого HTTP запроса.
     *
     * @param request  HTTP запрос от клиента
     * @param response HTTP ответ, который вернется клиенту
     * @param filterChain цепочка фильтров, которые должны выполниться после этого фильтра
     */
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Пытаемся получить заголовок Authorization из запроса
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Пример заголовка: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

        // 2. Проверяем, есть ли заголовок и начинается ли он с "Bearer "
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {

            // 3. Извлекаем сам токен (отрезаем "Bearer " в начале)
            String jwt = authHeader.substring(BEARER_PREFIX.length());

            try {
                // 4. Проверяем валидность токена через JwtTokenProvider
                if (!jwtTokenProvider.validateToken(jwt)) {
                    // Токен не прошел валидацию (неправильная подпись, испорчен и т.д.)
                    log.warn("Invalid or expired token");
                    handleError(response, HttpStatus.UNAUTHORIZED, "Token is invalid or expired");
                    return; // ВАЖНО: прекращаем выполнение, не вызываем filterChain.doFilter
                }

                // 5. Токен валидный - устанавливаем аутентификацию в SecurityContext
                setAuthentication(jwt);

            } catch (ExpiredJwtException e) {
                // Специфичная ошибка - токен просрочен (expired)
                log.warn("JWT expired: {}", e.getMessage());
                handleError(response, HttpStatus.UNAUTHORIZED, "Token expired");
                return;

            } catch (SignatureException e) {
                // Ошибка подписи - кто-то пытался подделать токен или изменить его
                log.warn("JWT signature invalid: {}", e.getMessage());
                handleError(response, HttpStatus.UNAUTHORIZED, "Invalid token signature");
                return;

            } catch (Exception e) {
                // Любая другая неожиданная ошибка при обработке токена
                log.error("Unexpected JWT processing error: {}", e.getMessage(), e);
                handleError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected token error");
                return;
            }
        } else {
            // Заголовок отсутствует или не начинается с Bearer
            log.debug("No JWT token found in request headers");
            // Не возвращаем ошибку - возможно, запрос к публичному endpoint'у
            // (например, /swagger-ui/, которые разрешены в SecurityConfig)
        }

        // 6. Продолжаем цепочку фильтров
        // Важно: этот метод вызывается ТОЛЬКО если не было ошибок и не вызван return раньше
        filterChain.doFilter(request, response);
    }

    /**
     * Устанавливает аутентификацию пользователя в Spring Security контекст.
     * Это говорит Spring'у, что пользователь аутентифицирован и имеет определенные роли.
     *
     * @param jwt валидный JWT токен
     */
    private void setAuthentication(String jwt) {
        // 1. Извлекаем имя пользователя (email) из токена
        String username = jwtTokenProvider.getUsername(jwt);

        // 2. Проверяем, что username получен и аутентификация еще не установлена
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 3. Получаем роли пользователя из токена
            // Optional.ofNullable - если роли null, то используем пустой список List.of()
            List<String> roles = Optional.ofNullable(jwtTokenProvider.getRoles(jwt)).orElse(List.of());

            // 4. Преобразуем строки ролей в объекты GrantedAuthority (требование Spring Security)
            // SimpleGrantedAuthority - простая реализация GrantedAuthority
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)  // для каждой строки роли создаем объект
                    .collect(Collectors.toList());      // собираем в список

            // 5. Создаем объект аутентификации Spring Security
            // параметры: principal (кто), credentials (пароль - null, т.к. уже проверили токен), authorities (роли)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 6. Устанавливаем эту аутентификацию в SecurityContext
            // Теперь Spring Security знает, что запрос выполняется от имени этого пользователя
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Логируем успешную аутентификацию (для отладки)
            log.debug("Authenticated user: {}, with roles: {}", username, roles);

            // Пример: если roles = ["ROLE_ADMIN", "ROLE_USER"]
            // То authorities = [ROLE_ADMIN, ROLE_USER]
            // Теперь можно использовать @PreAuthorize("hasRole('ADMIN')") в контроллерах
        }
    }

    /**
     * Вспомогательный метод для отправки ошибки клиенту.
     * Устанавливает HTTP статус и записывает сообщение в тело ответа.
     *
     * @param response HTTP ответ
     * @param status   HTTP статус ошибки (например, 401 Unauthorized)
     * @param message  Текст ошибки
     */
    private void handleError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());      // устанавливаем HTTP статус (например, 401)
        response.getWriter().write(message);     // пишем сообщение в тело ответа
        response.getWriter().flush();            // отправляем данные клиенту
        // Не закрываем Writer - это сделает контейнер Servlet
    }
}

/**
 * КАК РАБОТАЕТ ФИЛЬТР (ПОШАГОВО):
 *
 * 1. Клиент отправляет запрос: GET /actionLogs/all
 *    Заголовки: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * 2. Фильтр перехватывает запрос (до того, как он достигнет контроллера)
 *
 * 3. Проверяет заголовок Authorization:
 *    ✅ Если есть валидный токен:
 *       - Извлекает пользователя и роли
 *       - Устанавливает аутентификацию в SecurityContext
 *       - Запрос идет дальше к контроллеру
 *
 *    ❌ Если токен невалидный:
 *       - Возвращает 401 Unauthorized
 *       - Запрос НЕ доходит до контроллера
 *
 *    ⚠️ Если токена нет:
 *       - Запрос идет дальше (может быть публичный endpoint)
 *
 * 4. Контроллер получает запрос и может:
 *    - Получить текущего пользователя: SecurityContextHolder.getContext().getAuthentication()
 *    - Проверить роли через @PreAuthorize
 *
 * 5. После ответа контроллера фильтр очищает SecurityContext (автоматически)
 */

/**
 * ПРИМЕРЫ РАБОТЫ:
 *
 * Запрос: GET /actionLogs/all с токеном ADMIN
 * → Фильтр: токен валидный, роли [ROLE_ADMIN]
 * → SecurityContext: пользователь аутентифицирован как ADMIN
 * → Контроллер: возвращает все логи
 *
 * Запрос: GET /actionLogs/all с токеном USER
 * → Фильтр: токен валидный, роли [ROLE_USER]
 * → SecurityContext: пользователь аутентифицирован как USER
 * → Контроллер: @PreAuthorize("hasRole('ADMIN')") не пройдет - 403 Forbidden
 *
 * Запрос: GET /actionLogs/all с просроченным токеном
 * → Фильтр: ExpiredJwtException
 * → Ответ: 401 Unauthorized "Token expired"
 * → Контроллер даже не вызывается
 *
 * Запрос: GET /swagger-ui.html без токена
 * → Фильтр: токена нет, но пропускаем (permitAll в SecurityConfig)
 * → Контроллер Swagger: возвращает страницу
 */