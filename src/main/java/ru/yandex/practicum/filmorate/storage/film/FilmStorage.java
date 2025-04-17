package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

// Интерфейс FilmStorage определяет методы для работы с хранилищем фильмов
public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    // Метод findById для поиска фильма по ID
    Optional<Film> findById(Long id);

    // Метод findAll для получения всех фильмов
    Collection<Film> findAll();
}