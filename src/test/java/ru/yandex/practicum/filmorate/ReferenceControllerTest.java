package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.ReferenceController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.MpaRatingDao;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты для ReferenceController, который отвечает за справочные данные (жанры и рейтинги MPA).
 * Используется аннотация @WebMvcTest для тестирования только слоя контроллеров.
 * Все зависимости контроллера замоканы через @MockBean.
 */
@WebMvcTest(ReferenceController.class)
class ReferenceControllerTest {

    // MockMvc используется для выполнения HTTP-запросов к контроллеру в тестах
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper для преобразования объектов в JSON и обратно
    @Autowired
    private ObjectMapper objectMapper;

    // Мок-объект для DAO жанров
    @MockBean
    private GenreDao genreDao;

    // Мок-объект для DAO рейтингов MPA
    @MockBean
    private MpaRatingDao mpaRatingDao;

    /**
     * Метод выполняется перед каждым тестом.
     * Здесь можно подготовить необходимые данные или сбросить состояние.
     */
    @BeforeEach
    void setUp() {
        // Подготавливаем данные для тестов
    }

    /**
     * Проверяет получение всех жанров через GET-запрос /genres.
     */
    @Test
    void shouldGetAllGenres() throws Exception {
        List<Genre> genres = Arrays.asList(Genre.COMEDY, Genre.DRAMA);
        when(genreDao.findAll()).thenReturn(genres);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Драма"));
    }

    /**
     * Проверяет получение жанра по ID через GET-запрос /genres/{id}.
     */
    @Test
    void shouldGetGenreById() throws Exception {
        when(genreDao.findById(1)).thenReturn(Genre.COMEDY);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    /**
     * Проверяет, что при запросе несуществующего жанра возвращается ошибка 404.
     */
    @Test
    void shouldFailWhenGenreNotFound() throws Exception {
        when(genreDao.findById(999)).thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр с ID 999 не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Жанр с ID 999 не найден"));
    }

    /**
     * Проверяет получение всех рейтингов MPA через GET-запрос /mpa.
     */
    @Test
    void shouldGetAllMpaRatings() throws Exception {
        List<MpaRating> ratings = Arrays.asList(MpaRating.G, MpaRating.PG);
        when(mpaRatingDao.findAll()).thenReturn(ratings);

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("PG"));
    }

    /**
     * Проверяет получение рейтинга MPA по ID через GET-запрос /mpa/{id}.
     */
    @Test
    void shouldGetMpaRatingById() throws Exception {
        when(mpaRatingDao.findById(1)).thenReturn(MpaRating.G);

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));
    }

    /**
     * Проверяет, что при запросе несуществующего рейтинга MPA возвращается ошибка 404.
     */
    @Test
    void shouldFailWhenMpaRatingNotFound() throws Exception {
        when(mpaRatingDao.findById(999)).thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг MPA с ID 999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Рейтинг MPA с ID 999 не найден"));
    }
}