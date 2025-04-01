package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс FilmController отвечает за обработку HTTP-запросов, связанных с фильмами.
 */
@Slf4j // Аннотация для логирования событий
@RestController // Указывает, что это REST-контроллер, возвращающий данные в формате JSON
@RequestMapping("/films") // Все запросы к этому контроллеру начинаются с /films
public class FilmController {

    // Хранилище фильмов, где ключ — это ID фильма, а значение — объект Film
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping // Обрабатывает GET-запросы на "/films"
    public Collection<Film> findAll() {
        return films.values(); // Возвращает все фильмы из Map
    }

    @PostMapping // Обрабатывает POST-запросы на "/films"
    public Film create(@RequestBody Film film) {
        validateFilm(film); // Проверяем, что фильм соответствует требованиям
        film.setId(getNextId()); // Устанавливаем уникальный ID для нового фильма
        films.put(film.getId(), film); // Добавляем фильм в хранилище
        log.info("Фильм успешно добавлен"); // Записываем в лог сообщение об успехе
        return film; // Возвращаем созданный фильм клиенту

    }

    @PutMapping // Обрабатывает PUT-запросы на /films
    public Film updateFilm(@RequestBody Film film) {

        // Проверяем, указан ли ID фильма
        if (film.getId() == null) {
            log.error("Фильм с ID {} не найден", film.getId()); // Логируем ошибку
            throw new ValidationException("ID фильма должен быть указан"); // Выбрасываем исключение
        }

        // Проверяем, существует ли фильм с таким ID
        if (film.getId() <= 0 || !films.containsKey(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        validateFilm(film); // Проверяем, что данные фильма корректны
        films.put(film.getId(), film); // Обновляем фильм в хранилище
        log.info("Фильм успешно изменен");
        return film; // Возвращаем обновлённый фильм
    }


    // Метод для генерации следующего уникального ID.
    private long getNextId() {
        // Находим максимальный ID среди всех фильмов, если хранилище пустое то возвращаем 0
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId; // Увеличиваем максимальный ID на 1 и возвращаем
    }

    // Метод для проверки данных фильма перед созданием или обновлением.
    private void validateFilm(Film film) {
        // Проверяем, что название не пустое
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: Название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        // Проверяем, что описание не длиннее 200 символов (если оно есть)
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: Описание не может превышать 200 символов");
            throw new ValidationException("Описание не может превышать 200 символов");
        }

        // Проверяем, что дата релиза не раньше 28 декабря 1895 года (начала кинематографа)
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1985,
                12, 28))) {
            log.error("Ошибка валидации: Дата релиза фильма не может быть раньше 28 декабря 1985г.");
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1985г.");
        }

        // Проверяем, что продолжительность фильма положительная
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: Продолжительность фильма должна быть положительным числом");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
