package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с бизнес-логикой, связанной с фильмами.
 * Позволяет добавлять и удалять лайки, а также получать список популярных фильмов.
 */
@Service
@RequiredArgsConstructor
public class FilmService {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    // Хранилище фильмов, внедряется через конструктор (используется реализация filmDbStorage)
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    // Хранилище пользователей, внедряется через конструктор (используется реализация userDbStorage)
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    /**
     * Добавить лайк фильму от пользователя.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);

        // Получаем фильм или выбрасываем исключение, если не найден
        FilmDto film = getFilmOrThrow(filmId);
        // Проверяем, что пользователь существует
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        // Добавляем лайк и обновляем фильм в хранилище
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("Лайк добавлен фильму с ID {} от пользователя с ID {}", filmId, userId);
    }

    /**
     * Удалить лайк у фильма от пользователя.
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     */
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);

        // Получаем фильм или выбрасываем исключение, если не найден
        FilmDto film = getFilmOrThrow(filmId);
        // Проверяем, что пользователь существует
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        // Удаляем лайк и обновляем фильм в хранилище
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("Лайк удалён с фильма с ID {} от пользователя с ID {}", filmId, userId);
    }

    /**
     * Получить список самых популярных фильмов (по количеству лайков).
     * @param count количество фильмов в списке
     * @return список популярных фильмов
     */
    public List<FilmDto> getPopularFilms(Integer count) {
        log.debug("Получение списка популярных фильмов (количество: {})", count);

        // Получаем все фильмы из хранилища
        List<FilmDto> films = new ArrayList<>(filmStorage.findAll());
        // Сортируем фильмы по количеству лайков в убывающем порядке и ограничиваем по count
        List<FilmDto> popularFilms = films.stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());

        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    /**
     * Вспомогательный метод: получить фильм по ID или выбросить исключение, если не найден.
     * @param filmId идентификатор фильма
     * @return FilmDto
     */
    private FilmDto getFilmOrThrow(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }
}