package ru.yandex.practicum.filmorate.model;

import lombok.Data;

/**
 * DTO (Data Transfer Object) для передачи информации о жанре фильма между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
public class GenreDto {
    // Уникальный идентификатор жанра
    private Integer id;
    // Название жанра
    private String name;
}