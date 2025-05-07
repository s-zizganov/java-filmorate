package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
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
    private Set<Long> likes;
    // Список идентификаторов жанров фильма
    private List<Integer> genres; // Список ID жанров
    // Идентификатор рейтинга MPA
    private String mpa; // ID рейтинга MPA
}