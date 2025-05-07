package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * REST-контроллер для управления фильмами: создание, обновление, получение, лайки и популярные фильмы.
 */
@RestController
@RequestMapping(FilmController.BASE_PATH)
@RequiredArgsConstructor
public class FilmController {
    // Базовый путь для всех эндпоинтов, связанных с фильмами
    public static final String BASE_PATH = "/films";
    // Путь для операций с лайками
    private static final String LIKE_PATH = "/{id}/like/{userId}";
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    // Хранилище фильмов, внедряется через конструктор (используется реализация filmDbStorage)
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    // Сервис для работы с бизнес-логикой фильмов
    private final FilmService filmService;

    // Приватный метод для валидации данных фильма
    private void validateFilm(FilmDto film) {
        log.debug("Начало валидации фильма: {}", film);
        // Проверка, что название фильма не пустое
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: Название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        // Проверка, что описание не превышает 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: Описание не может превышать 200 символов");
            throw new ValidationException("Описание не может превышать 200 символов");
        }
        // Проверка, что дата релиза не раньше 28 декабря 1895 года
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: Дата релиза фильма не может быть раньше 28 декабря 1895г.");
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895г.");
        }
        // Проверка, что продолжительность фильма положительная
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: Продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма успешно завершена");
    }

    // Получение всех фильмов (GET /films)
    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        Collection<FilmDto> films = filmStorage.findAll();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    // Получение фильма по ID (GET /films/{id})
    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable("id") Long filmId) {
        log.info("Получен запрос на получение фильма с ID {}", filmId);
        // Поиск фильма по ID, если не найден — выбрасывается исключение
        FilmDto film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        log.debug("Найден фильм: {}", film);
        return film;
    }

    // Создание нового фильма (POST /films)
    @PostMapping
    public FilmDto create(@RequestBody FilmDto film) {
        log.info("Получен запрос на создание фильма: {}", film);
        // Валидация данных фильма
        validateFilm(film);
        // Сохранение фильма в хранилище
        FilmDto createdFilm = filmStorage.create(film);
        log.info("Фильм успешно добавлен с ID {}", createdFilm.getId());
        return createdFilm;
    }

    // Обновление существующего фильма (PUT /films)
    @PutMapping
    public FilmDto updateFilm(@RequestBody FilmDto film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        // Проверка, что тело запроса не пустое
        if (film == null) {
            log.error("Тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        // Проверка, что указан ID фильма
        if (film.getId() == null) {
            log.error("ID фильма должен быть указан");
            throw new ValidationException("ID фильма должен быть указан");
        }
        // Проверка, что фильм с таким ID существует
        FilmDto existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с ID %d не найден", film.getId())));
        log.debug("Существующий фильм: {}", existingFilm);
        // Валидация новых данных фильма
        validateFilm(film);
        // Обновление фильма в хранилище
        FilmDto updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID {} успешно обновлён", updatedFilm.getId());
        return updatedFilm;
    }

    // Добавление лайка фильму (PUT /films/{id}/like/{userId})
    @PutMapping(LIKE_PATH)
    public void addLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: filmId={}, userId={}", filmId, userId);
        // Добавление лайка фильму через сервис
        filmService.addLike(filmId, userId);
        log.info("Лайк успешно добавлен");
    }

    // Удаление лайка у фильма (DELETE /films/{id}/like/{userId})
    @DeleteMapping(LIKE_PATH)
    public void removeLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: filmId={}, userId={}", filmId, userId);
        // Удаление лайка через сервис
        filmService.removeLike(filmId, userId);
        log.info("Лайк успешно удалён");
    }

    // Получение списка популярных фильмов (GET /films/popular?count=N)
    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение популярных фильмов, count={}", count);
        // Получение популярных фильмов через сервис
        List<FilmDto> popularFilms = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}