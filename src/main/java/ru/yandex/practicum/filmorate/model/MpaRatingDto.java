package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * DTO (Data Transfer Object) для передачи информации о рейтинге MPA между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MpaRatingDto {
    // Идентификатор рейтинга MPA
    Integer id;
    // Название рейтинга MPA
    String name;
}