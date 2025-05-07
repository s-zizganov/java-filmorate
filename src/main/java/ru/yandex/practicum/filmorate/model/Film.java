package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс Film представляет модель фильма в приложении Filmorate.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    Set<Long> likes = new HashSet<>(); // Список ID пользователей,
    // которые поставили лайк фильму (тип Set<Long>)
    // Инициализируем его как пустой HashSet, чтобы избежать NullPointerException
    List<Genre> genres = new ArrayList<>();
    MpaRating mpa;
}
