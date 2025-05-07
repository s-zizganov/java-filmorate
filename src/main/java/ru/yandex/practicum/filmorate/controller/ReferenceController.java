package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;
import ru.yandex.practicum.filmorate.storage.film.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.MpaRatingDao;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для получения справочной информации о жанрах и рейтингах MPA.
 */
@RestController
@RequiredArgsConstructor
public class ReferenceController {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(ReferenceController.class);
    // DAO для работы с жанрами фильмов
    private final GenreDao genreDao;
    // DAO для работы с рейтингами MPA
    private final MpaRatingDao mpaRatingDao;

    // Получение списка всех жанров (GET /genres)
    @GetMapping("/genres")
    public List<GenreDto> getAllGenres() {
        log.info("Получен запрос на получение всех жанров");
        // Получаем все жанры из DAO
        List<Genre> genres = genreDao.findAll();
        // Преобразуем список Genre в список GenreDto для передачи клиенту
        List<GenreDto> genreDtos = genres.stream()
                .map(genre -> {
                    GenreDto dto = new GenreDto();
                    dto.setId(genre.ordinal() + 1); // ID соответствует порядку в enum, начиная с 1
                    dto.setName(genre.getName());
                    return dto;
                })
                .collect(Collectors.toList());
        log.info("Возвращено {} жанров", genreDtos.size());
        return genreDtos;
    }

    // Получение жанра по ID (GET /genres/{id})
    @GetMapping("/genres/{id}")
    public GenreDto getGenreById(@PathVariable Integer id) {
        log.info("Получен запрос на получение жанра с ID {}", id);
        // Получаем жанр по ID из DAO
        Genre genre = genreDao.findById(id);
        // Преобразуем Genre в GenreDto
        GenreDto dto = new GenreDto();
        dto.setId(id);
        dto.setName(genre.getName());
        log.info("Найден жанр: {}", dto);
        return dto;
    }

    // Получение списка всех рейтингов MPA (GET /mpa)
    @GetMapping("/mpa")
    public List<MpaRatingDto> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        // Получаем все рейтинги из DAO
        List<MpaRating> ratings = mpaRatingDao.findAll();
        // Преобразуем список MpaRating в список MpaRatingDto для передачи клиенту
        List<MpaRatingDto> ratingDtos = ratings.stream()
                .map(rating -> {
                    MpaRatingDto dto = new MpaRatingDto();
                    dto.setId(rating.ordinal() + 1); // ID соответствует порядку в enum, начиная с 1
                    dto.setName(rating.getRating());
                    return dto;
                })
                .collect(Collectors.toList());
        log.info("Возвращено {} рейтингов MPA", ratingDtos.size());
        return ratingDtos;
    }

    // Получение рейтинга MPA по ID (GET /mpa/{id})
    @GetMapping("/mpa/{id}")
    public MpaRatingDto getMpaRatingById(@PathVariable Integer id) {
        log.info("Получен запрос на получение рейтинга MPA с ID {}", id);
        // Получаем рейтинг по ID из DAO
        MpaRating rating = mpaRatingDao.findById(id);
        // Преобразуем MpaRating в MpaRatingDto
        MpaRatingDto dto = new MpaRatingDto();
        dto.setId(id);
        dto.setName(rating.getRating());
        log.info("Найден рейтинг MPA: {}", dto);
        return dto;
    }
}