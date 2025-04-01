package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class) // Указывает, что тестируем только FilmController в изолированной среде
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc; // Инструмент для отправки HTTP-запросов в тестах

    @Autowired
    private ObjectMapper objectMapper; // Преобразует объекты Java в JSON и обратно

    @Autowired
    private FilmController filmController; // Добавляем контроллер, чтобы очищать хранилище


    // Очищает хранилище фильмов перед каждым тестом, чтобы тесты не влияли друг на друга.
    @BeforeEach
    void setUp() {
        // Очищаем хранилище перед каждым тестом
        filmController.findAll().clear();
    }

    // Тест на успешное создание фильма.
    @Test
    void shouldCreateFilmSuccessfully() throws Exception {
        // Создаём объект фильма с корректными данными
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Description1");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        // Отправляем POST-запрос на "/films" с фильмом в формате JSON
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON) // Указываем, что отправляем JSON
                        .content(objectMapper.writeValueAsString(film))) // Преобразуем фильм в JSON
                .andExpect(status().isOk()) // Ожидаем статус 200 OK
                .andExpect(jsonPath("$.id").value(1)) // Проверяем, что ID равен 1 (первый фильм)
                .andExpect(jsonPath("$.name").value("Film1")) // Проверяем название
                .andExpect(jsonPath("$.description").value("Description1")) // Проверяем описание
                .andExpect(jsonPath("$.releaseDate").value("2010-07-16")) // Проверяем дату релиза
                .andExpect(jsonPath("$.duration").value(148)); // Проверяем продолжительность
    }

    // Тест на успешное обновление фильма.
    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {
        // Создаём фильм
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Description1");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        // Отправляем POST-запрос и сохраняем ответ
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(); // Получаем JSON ответа

        Film createdFilm = objectMapper.readValue(response, Film.class); // Преобразуем ответ в объект Film

        // Обновляем фильм
        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Film2");
        updatedFilm.setDescription("Description2");
        updatedFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        updatedFilm.setDuration(150);

        // Отправляем PUT-запрос для обновления
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value("Film2"))
                .andExpect(jsonPath("$.description").value("Description2"))
                .andExpect(jsonPath("$.releaseDate").value("2010-07-16"))
                .andExpect(jsonPath("$.duration").value(150));
    }

    // Тест на получение списка фильмов.
    @Test
    void shouldGetAllFilms() throws Exception {
        // Создаём фильм
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("Description1");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());

        // Проверяем список фильмов
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Film1"));
    }

    // Тест на создание фильма с пустым названием.
    @Test
    void shouldFailWhenNameIsBlank() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400
                .andExpect(jsonPath("$.error").value("Validation error")) // Тип ошибки
                .andExpect(jsonPath("$.message").value("Название не может быть пустым"));
    }

    // Тест на создание фильма с описанием длиннее 200 символов.
    @Test
    void shouldFailWhenDescriptionIsTooLong() throws Exception {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201)); // Создаём строку из 201 символа A
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Описание не может превышать 200 символов"));
    }

    // Тест на создание фильма с датой релиза раньше 28 декабря 1895 года
    @Test
    void shouldFailWhenReleaseDateIsTooEarly() throws Exception {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1985, 12, 27));
        film.setDuration(120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Дата релиза фильма не может быть раньше " +
                        "28 декабря 1985г."));
    }

    // Тест на создание фильма с отрицательной продолжительностью.
    @Test
    void shouldFailWhenDurationIsNegative() throws Exception {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Продолжительность фильма должна быть положительным числом"));
    }

    // Тест на отправку пустого запроса.
    @Test
    void shouldFailWhenRequestIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")) // Пустое тело запроса
                .andExpect(status().isBadRequest()); // Ожидаем статус 400
    }

    // Тест на обновление фильма с null ID.
    @Test
    void shouldFailWhenUpdatingWithNullId() throws Exception {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setId(null); // ID не указан

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("ID фильма должен быть указан"));
    }
}