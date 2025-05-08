package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.FilmDto;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс для работы с хранилищем фильмов.
 * Определяет основные методы для создания, обновления, удаления, поиска и получения фильмов.
 */
public interface FilmStorage {
    /**
     * Создать новый фильм.
     * @param film объект фильма
     * @return созданный фильм
     */
    FilmDto create(FilmDto film);

    /**
     * Обновить существующий фильм.
     * @param film объект фильма с обновлёнными данными
     * @return обновлённый фильм
     */
    FilmDto update(FilmDto film);

    /**
     * Удалить фильм по идентификатору.
     * @param id идентификатор фильма
     */
    void delete(Long id);

    /**
     * Найти фильм по идентификатору.
     * @param id идентификатор фильма
     * @return Optional с фильмом, если найден
     */
    Optional<FilmDto> findById(Long id);

    /**
     * Получить все фильмы.
     * @return коллекция всех фильмов
     */
    Collection<FilmDto> findAll();

    /**
     * Проверка существования рейтинга MPA.
     * @param mpaId идентификатор рейтинга MPA
     * @return true, если рейтинг существует, иначе false
     */
    boolean existsMpa(Integer mpaId);

    /**
     * Проверка существования жанра.
     * @param genreId идентификатор жанра
     * @return true, если жанр существует, иначе false
     */
    boolean existsGenre(Integer genreId);
}