package ru.yandex.practicum.filmorate.model;

import lombok.Data;

/**
 * DTO (Data Transfer Object) для передачи информации о рейтинге MPA между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
public class MpaRatingDto {
    // Идентификатор рейтинга MPA
    private String id;
    // Название рейтинга MPA
    private String name;
}