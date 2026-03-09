package com.post_hub.utils_service.security;

import com.post_hub.utils_service.security.model.AuthenticationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;

@Slf4j  // Lombok аннотация для автоматического создания логгера (поле log)
@Component  // Spring компонент - автоматически создаст бин этого класса
public class JwtTokenProvider {

    /**
     * Секретный ключ для подписи и проверки JWT токенов.
     * Хранится в зашифрованном виде в памяти.
     */
    private final SecretKey secretKey;

    /**
     * Конструктор, который принимает секретный ключ из application.properties
     * и преобразует его из строки Base64 в объект SecretKey для использования с JWT.
     *
     * @param secret секретный ключ в Base64 кодировке (из файла свойств)
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // 1. Декодируем строку из Base64 в массив байтов
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        // 2. Создаем HMAC-SHA ключ из массива байтов (алгоритм определяется автоматически)
        //    Для HS256 нужно минимум 32 байта (256 бит)
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

        // Логируем успешную инициализацию (без самого ключа в целях безопасности)
        log.debug("JWT секретный ключ успешно инициализирован");
    }

    /**
     * Извлекает имя пользователя (subject) из JWT токена.
     * Subject - это обычно email или логин пользователя.
     *
     * @param token JWT токен в формате строки
     * @return имя пользователя (subject) из токена
     */
    public String getUsername(String token) {
        // getAllClaimsFromToken(token) - получает все данные из токена
        // getSubject() - извлекает стандартное поле "sub" (subject)
        return getAllClaimsFromToken(token).getSubject();
    }

    /**
     * Извлекает список ролей пользователя из JWT токена.
     * Роли хранятся в кастомном поле, определенном в AuthenticationConstants.ROLE.
     *
     * @param token JWT токен в формате строки
     * @return список ролей пользователя (может быть пустым, если ролей нет)
     */
    public List<String> getRoles(String token) {
        // Получаем значение поля с ролями из токена
        Object rawRoles = getAllClaimsFromToken(token).get(AuthenticationConstants.ROLE);

        // Проверяем, что это действительно список
        if (rawRoles instanceof List<?>) {
            // Преобразуем каждый элемент списка в строку и собираем в новый список
            return ((List<?>) rawRoles).stream()
                    .filter(String.class::isInstance)      // оставляем только строки
                    .map(String.class::cast)               // преобразуем в String
                    .toList();                              // собираем в список
        }

        // Если ролей нет или формат неправильный - возвращаем пустой список
        return List.of();
    }

    /**
     * Проверяет валидность JWT токена.
     * Валидным считается токен, который:
     * - имеет правильную подпись (соответствует нашему секретному ключу)
     * - не просрочен (если установлена дата истечения)
     * - имеет корректную структуру JWT
     *
     * @param token JWT токен для проверки
     * @return true - если токен валидный, false - если нет
     */
    public boolean validateToken(String token) {
        try {
            // Пытаемся распарсить и проверить токен
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)      // устанавливаем ключ для проверки подписи
                    .build()                        // создаем парсер
                    .parseClaimsJws(token);         // парсим токен и проверяем подпись

            // Если дошли до этой строки - исключений не было, значит токен валидный
            log.debug("JWT токен успешно проверен");
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            // JwtException - ошибки, связанные с JWT (неправильная подпись, просрочен и т.д.)
            // IllegalArgumentException - ошибки формата (пустой токен, неправильная структура)
            log.warn("Проверка JWT токена не пройдена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Вспомогательный приватный метод для получения всех claims (утверждений) из токена.
     * Claims - это данные, которые хранятся в теле JWT токена.
     *
     * @param token JWT токен
     * @return объект Claims со всеми данными из токена
     * @throws JwtException если токен невалидный (пробрасывается вызывающему методу)
     */
    private Claims getAllClaimsFromToken(String token) {
        // Парсим токен и извлекаем его тело (payload)
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)      // ключ для проверки подписи (обязательно!)
                .build()                        // создаем парсер
                .parseClaimsJws(token)           // парсим и проверяем токен
                .getBody();                      // получаем claims (тело токена)
    }
}

/**
 * КЛАСС ДЛЯ ПОНИМАНИЯ СТРУКТУРЫ JWT ТОКЕНА:
 *
 * JWT состоит из трех частей, разделенных точками:
 * HEADER.PAYLOAD.SIGNATURE
 *
 * 1. HEADER (заголовок):
 *    {
 *      "alg": "HS256",           // алгоритм подписи
 *      "typ": "JWT"               // тип токена
 *    }
 *
 * 2. PAYLOAD (полезная нагрузка) - это и есть Claims:
 *    {
 *      "sub": "user@email.com",   // subject (кто пользователь)
 *      "roles": ["ROLE_USER"],    // кастомное поле с ролями
 *      "userId": 123,             // кастомное поле с ID пользователя
 *      "iat": 1516239022,         // issued at (когда выпущен)
 *      "exp": 1516242622          // expiration (когда истекает)
 *    }
 *
 * 3. SIGNATURE (подпись):
 *    Создается путем шифрования header + payload с использованием секретного ключа
 *    Именно подпись гарантирует, что токен не был изменен
 */