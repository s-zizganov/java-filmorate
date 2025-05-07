package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO (Data Transfer Object) для передачи информации о фильме между слоями приложения.
 * Используется для обмена данными с клиентом и между сервисами.
 */
@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    // Множество идентификаторов пользователей, поставивших лайк фильму
    private Set<Long> likes = new HashSet<>();
    // Список объектов жанров
    private List<GenreDto> genres;
    // Объект рейтинга MPA
    private MpaRatingDto mpa;
}