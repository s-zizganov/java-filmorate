package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

// Аннотация @Slf4j автоматически добавляет логгер с именем log
@Slf4j
// Аннотация @RestController указывает, что этот класс является REST-контроллером, который обрабатывает HTTP-запросы
@RestController
// Аннотация @RequestMapping /films указывает, что все запросы, начинающиеся с /films, будут обрабатываться этим
// контроллером
@RequestMapping(FilmController.BASE_PATH)
public class FilmController {
    // Константа BASE_PATH задаёт базовый путь для всех методов контроллера
    public static final String BASE_PATH = "/films";
    private static final String LIKE_PATH = "/{id}/like/{userId}";
    // Объявляем переменную filmStorage для работы с хранилищем фильмов
    private final FilmStorage filmStorage;
    // Объявляем переменную filmService для работы с сервисом фильмов
    private final FilmService filmService;

    // Конструктор класса FilmController, который принимает зависимости через аннотацию @Autowired
    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        // Присваиваем переданное хранилище filmStorage локальной переменной filmStorage
        this.filmStorage = filmStorage;
        // Присваиваем переданный сервис filmService локальной переменной filmService
        this.filmService = filmService;
    }

    // Метод validateFilm для проверки данных фильма перед сохранением или обновлением
    private void validateFilm(Film film) {
        log.debug("Начало валидации фильма: {}", film);
        // Проверяем, что название фильма не пустое (не null и не состоит только из пробелов)
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: Название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверяем, что описание фильма (если оно есть) не превышает 200 символов
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: Описание не может превышать 200 символов");
            throw new ValidationException("Описание не может превышать 200 символов");
        }

        // Проверяем, что дата релиза фильма не раньше 28.12/1895 года
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: Дата релиза фильма не может быть раньше 28 декабря 1895г.");
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895г.");
        }

        // Проверяем, что продолжительность фильма больше 0
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: Продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        log.debug("Валидация фильма успешно завершена");
    }

    // Метод findAll для получения всех фильмов, обрабатывает GET-запрос на /films
    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        // Получаем все фильмы из хранилища filmStorage, метод findAll возвращает коллекцию фильмов
        Collection<Film> films = filmStorage.findAll();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    // Метод findById для получения фильма по ID, обрабатывает GET-запрос на /films/id
    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") Long filmId) {
        log.info("Получен запрос на получение фильма с ID {}", filmId);
        // Ищем фильм в хранилище по ID, метод findById возвращает Optional<Film>
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        log.debug("Найден фильм: {}", film);
        return film;
    }

    // Метод create для создания нового фильма, обрабатывает POST-запрос на /films
    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        validateFilm(film);
        // Сохраняем фильм в хранилище, метод crate возвращает созданный фильм с установленным ID
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно добавлен с ID {}", createdFilm.getId());
        return createdFilm;
    }

    // Метод updateFilm для обновления существующего фильма (обрабатывает PUT-запрос на /films)
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        // Проверяем, что тело запроса не пустое
        if (film == null) {
            log.error("Тело запроса не может быть пустым");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        // Проверяем, что ID фильма указан
        if (film.getId() == null) {
            log.error("ID фильма должен быть указан");
            throw new ValidationException("ID фильма должен быть указан");
        }

        // Ищем фильм в хранилище по id, чтобы проверить, что он существует
        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с ID %d не найден", film.getId())));
        log.debug("Существующий фильм: {}", existingFilm);

        validateFilm(film);
        // Обновляем фильм в хранилище, метод update возвращает обновлённый фильм
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм с ID {} успешно обновлён", updatedFilm.getId());
        return updatedFilm;
    }

    // Метод addLike для добавления лайка фильму, обрабатывает PUT-запрос на /films/id/like/userId
    @PutMapping(LIKE_PATH)
    public void addLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: filmId={}, userId={}", filmId, userId);
        // Вызываем метод addLike в FilmService, чтобы добавить лайк фильму от пользователя
        filmService.addLike(filmId, userId);
        log.info("Лайк успешно добавлен");
    }

    // Метод removeLike для удаления лайка с фильма (обрабатывает DELETE-запрос на /films/id/like/userId
    @DeleteMapping(LIKE_PATH)
    public void removeLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: filmId={}, userId={}", filmId, userId);
        // Вызываем метод removeLike в FilmService чтобы удалить лайк
        filmService.removeLike(filmId, userId);
        log.info("Лайк успешно удалён");
    }

    // Метод getPopularFilms для получения списка популярных фильмов обрабатывает GET-запрос на /films/popular
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение популярных фильмов, count={}", count);
        // Вызываем метод getPopularFilms в FilmService чтобы получить список фильмов сортированных по количеству лайков
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}