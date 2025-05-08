package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * DTO (Data Transfer Object) для передачи информации о жанре фильма между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreDto {
    // Уникальный идентификатор жанра
    Integer id;
    // Название жанра
    String name;
}