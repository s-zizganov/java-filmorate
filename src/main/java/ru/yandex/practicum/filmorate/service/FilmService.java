package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service // Аннотация @Service указывает, что этот класс является сервисом в Spring (содержит бизнес-логику)
public class FilmService {

    // Объявляем переменную filmStorage для работы с хранилищем фильмов, это интерфейс FilmStorage
    private final FilmStorage filmStorage;
    // Переменная userStorage для работы с хранилищем пользователей
    private final UserStorage userStorage;

    // Создаём компаратор LIKES_COMPARATOR для сортировки фильмов по количеству лайков (в порядке убывания)
    private static final Comparator<Film> LIKES_COMPARATOR =
            // Сравниваем два фильма f1 и f2 по количеству лайков
            (f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size());

    // Конструктор класса FilmService, который принимает зависимости filmStorage и userStorage
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    // Метод getFilmOrThrow для получения фильма по id или выброса исключения, если фильм не найден
    private Film getFilmOrThrow(Long filmId) {
        log.debug("Поиск фильма с ID {}", filmId);
        // Если фильм не найден, выбрасываем исключение NotFoundException
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        log.debug("Фильм найден: {}", film);
        // Возвращаем найденный фильм
        return film;
    }

    // Метод getUserOrThrow для получения пользователя по Id или выброса исключения, если пользователь не найден
    private User getUserOrThrow(Long userId) {
        log.debug("Поиск пользователя с ID {}", userId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        log.debug("Пользователь найден: {}", user);
        return user;
    }

    // Метод addLike для добавления лайка фильму от пользователя
    public void addLike(Long filmId, Long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);
        // Получаем фильм по ID, используя метод getFilmOrThrow (если фильм не найден, будет выброшено исключение)
        Film film = getFilmOrThrow(filmId);
        // Получаем пользователя по ID, используя метод getUserOrThrow
        getUserOrThrow(userId);

        // Добавляем ID пользователя в список лайков фильма
        film.getLikes().add(userId);
        // Обновляем фильм в хранилище
        filmStorage.update(film);
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    // Метод removeLike для удаления лайка с фильма от пользователя
    public void removeLike(Long filmId, Long userId) {
        log.debug("Удаление лайка: filmId={}, userId={}", filmId, userId);
        // Получаем фильм по ID
        Film film = getFilmOrThrow(filmId);
        // Получаем пользователя по ID
        getUserOrThrow(userId);

        // Удаляем ID пользователя из списка лайков фильма
        film.getLikes().remove(userId);
        // Обновляем фильм в хранилище
        filmStorage.update(film);
        log.info("Пользователь с ID {} удалил лайк с фильма с ID {}", userId, filmId);
    }

    // Метод getPopularFilms для получения списка популярных фильмов, сортированных по количеству лайков
    public List<Film> getPopularFilms(int count) {
        log.debug("Получение популярных фильмов, count={}", count);
        // Проверяем, что параметр count больше 0
        if (count <= 0) {
            log.error("Количество фильмов должно быть положительным числом: {}", count);
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        // Получаем все фильмы из хранилища, сортируем их в порядке убывания лайков
        // и берём первые count фильмов
        List<Film> popularFilms = filmStorage.findAll().stream()
                // Сортируем фильмы с помощью компаратора LIKES_COMPARATOR
                .sorted(LIKES_COMPARATOR)
                // Ограничиваем количество фильмов параметром count
                .limit(count)
                // Преобразуем Stream в List
                .collect(Collectors.toList());
        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}