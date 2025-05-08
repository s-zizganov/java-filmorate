package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO (Data Transfer Object) для передачи информации о фильме между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmDto {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    // Множество идентификаторов пользователей, поставивших лайк фильму
    Set<Long> likes = new HashSet<>();
    // Список объектов жанров
    List<GenreDto> genres;
    // Объект рейтинга MPA
    MpaRatingDto mpa;
}