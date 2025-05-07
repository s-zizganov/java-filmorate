package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.FilmDto;

import java.util.*;

// Класс InMemoryFilmStorage реализует интерфейс FilmStorage, храня фильмы в памяти (в HashMap)
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, FilmDto> films = new HashMap<>();

    // Метод для создания нового фильма
    @Override
    public FilmDto create(FilmDto film) {
        film.setId(getNextId()); // Устанавливаем уникальный ID для нового фильма
        films.put(film.getId(), film); // Добавляем фильм в хранилище
        return film;
    }

    // Метод для обновления существующего фильма
    @Override
    public FilmDto update(FilmDto film) {
        films.put(film.getId(), film); // Обновляем фильм в хранилище
        return film;
    }

    // Метод для удаления фильма по ID
    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    // Метод для поиска фильма по ID
    @Override
    public Optional<FilmDto> findById(Long id) {
        // Ищем фильм в HashMap по ID и оборачиваем результат в Optional
        // Если фильм не найден, Optional будет пустым
        return Optional.ofNullable(films.get(id));
    }

    // Метод для получения всех фильмов
    @Override
    public Collection<FilmDto> findAll() {
        return films.values();
    }

    // Метод для генерации следующего уникального ID.
    private long getNextId() {
        // Находим максимальный ID среди всех фильмов, если хранилище пустое то возвращаем 0
        long currentMaxId = films.keySet()
                .stream()
                // Преобразуем каждый ключ в число типа long
                .mapToLong(id -> id)
                // Находим максимальный ID, если хранилище пустое — возвращаем 0
                .max()
                .orElse(0);
        return ++currentMaxId; // Увеличиваем максимальный ID на 1 и возвращаем
    }
}