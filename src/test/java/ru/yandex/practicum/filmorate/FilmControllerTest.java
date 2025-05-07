package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Аннотация @WebMvcTest указывает, что мы тестируем контроллер FilmController
// Spring загрузит только компоненты, связанные с MVC (контроллеры, обработчики исключений и т.д.), а остальные
// зависимости замокит
@WebMvcTest(FilmController.class)
class FilmControllerTest {

    // Переменная mockMvc для выполнения HTTP-запросов в тестах
    // @Autowired автоматически внедряет MockMvc, созданный Spring для тестирования
    @Autowired
    private MockMvc mockMvc;

    // Переменная objectMapper для преобразования объектов в JSON и обратно
    // @Autowired автоматически внедряет ObjectMapper, созданный Spring
    @Autowired
    private ObjectMapper objectMapper;

    // Мок объект filmStorage для имитации хранилища фильмов
    // @MockBean создаёт мок и добавляет его в контекст Spring, заменяя реальное хранилище
    @MockBean(name = "filmDbStorage")
    private FilmStorage filmStorage;

    // Мок-объект filmService для имитации сервиса фильмов
    // @MockBean создаёт мок и добавляет его в контекст Spring, заменяя реальный сервис
    @MockBean
    private FilmService filmService;

    @MockBean(name = "userDbStorage")
    private UserStorage userStorage;

    // Переменная film для хранения тестового объекта FilmDto, который будем использовать в тестах
    private FilmDto film;

    // Метод setUp выполняется перед каждым тестом (аннотация @BeforeEach)
    // Он подготавливает тестовые данные, которые будут использоваться в тестах
    @BeforeEach
    void setUp() {
        // Создаём новый объект FilmDto для тестов
        film = new FilmDto();
        film.setName("Test Film");
        film.setDescription("A test film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        // Добавляем обязательный MPA-рейтинг
        MpaRatingDto mpa = new MpaRatingDto();
        mpa.setId(3); // Соответствует "PG-13" (как в FilmoRateIntegrationTests)
        mpa.setName("PG-13");
        film.setMpa(mpa);

        // Добавляем жанры
        GenreDto genre = new GenreDto();
        genre.setId(1); // Соответствует "Комедия" (как в FilmoRateIntegrationTests)
        genre.setName("Комедия");
        film.setGenres(Collections.singletonList(genre));
    }

    @Test // Проверяет, что фильм можно успешно получить по ID через GET-запрос
    void shouldGetFilmById() throws Exception {
        // Устанавливаем ID фильма равным 1
        film.setId(1L);
        // Настраиваем мок filmStorage: при вызове метода findById с ID = 1 возвращаем Optional с нашим тестовым фильмом
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));

        // Выполняем GET-запрос на /films/1 через MockMvc, чтобы получить фильм с ID = 1
        mockMvc.perform(get("/films/1"))
                // Проверяем, что статус ответа — 200 (OK), то есть запрос прошёл успешно
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе поле id равно 1
                .andExpect(jsonPath("$.id").value(1))
                // Проверяем, что в JSON-ответе поле name равно "Test Film"
                .andExpect(jsonPath("$.name").value("Test Film"))
                // Проверяем, что в JSON-ответе поле description равно "A test film description"
                .andExpect(jsonPath("$.description").value("A test film description"))
                // Проверяем, что в JSON-ответе поле releaseDate равно "2000-01-01"
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                // Проверяем, что в JSON-ответе поле duration равно 120
                .andExpect(jsonPath("$.duration").value(120))
                // Проверяем, что MPA-рейтинг возвращается корректно
                .andExpect(jsonPath("$.mpa.id").value(3))
                .andExpect(jsonPath("$.mpa.name").value("PG-13"))
                // Проверяем, что жанры возвращаются корректно
                .andExpect(jsonPath("$.genres[0].id").value(1))
                .andExpect(jsonPath("$.genres[0].name").value("Комедия"));
    }

    @Test // Проверяет, что при запросе несуществующего фильма возвращается ошибка 404
    void shouldFailWhenFilmNotFoundById() throws Exception {
        // Настраиваем мок filmStorage: при вызове findById с ID = 999 возвращаем пустой Optional (фильм не найден)
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        // Выполняем GET-запрос на /films/999 через MockMvc, чтобы получить фильм с id = 999
        mockMvc.perform(get("/films/999"))
                // Проверяем, что статус ответа — 404 (Not Found), так как фильм не найден
                .andExpect(status().isNotFound())
                // Проверяем, что в JSON-ответе поле error равно "Not found"
                .andExpect(jsonPath("$.error").value("Not found"))
                // Проверяем, что в JSON-ответе поле message равно "Фильм с ID 999 не найден"
                .andExpect(jsonPath("$.message").value("Фильм с ID 999 не найден"));
    }

    @Test // Проверяет, что фильм можно успешно создать через POST-запрос
    void shouldCreateFilmSuccessfully() throws Exception {
        // Настраиваем мок filmStorage: при вызове create с любым фильмом (any(FilmDto.class)) возвращаем фильм с id = 1
        when(filmStorage.create(any(FilmDto.class))).thenAnswer(invocation -> {
            // Получаем фильм, который передали в метод create
            FilmDto filmToCreate = invocation.getArgument(0);
            // Устанавливаем ID фильма равным 1
            filmToCreate.setId(1L);
            // Возвращаем фильм с установленным Id
            return filmToCreate;
        });

        // Выполняем POST-запрос на /films через MockMvc, чтобы создать новый фильм
        mockMvc.perform(post("/films")
                        // Указываем, что тело запроса — это JSON (тип содержимого — application/json)
                        .contentType(MediaType.APPLICATION_JSON)
                        // Преобразуем тестовый фильм в JSON-строку и отправляем в теле запроса
                        .content(objectMapper.writeValueAsString(film)))
                // Проверяем, что статус ответа — 200 (OK), то есть фильм успешно создан
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе поле id равно 1
                .andExpect(jsonPath("$.id").value(1))
                // Проверяем, что в JSON-ответе поле name равно "Test Film"
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("A test film description"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(120))
                // Проверяем, что MPA-рейтинг возвращается корректно
                .andExpect(jsonPath("$.mpa.id").value(3))
                .andExpect(jsonPath("$.mpa.name").value("PG-13"))
                // Проверяем, что жанры возвращаются корректно
                .andExpect(jsonPath("$.genres[0].id").value(1))
                .andExpect(jsonPath("$.genres[0].name").value("Комедия"));
    }

    @Test  // Проверяет, что существующий фильм можно успешно обновить через PUT-запрос
    void shouldUpdateFilmSuccessfully() throws Exception {
        // Настраиваем мок filmStorage: при вызове create с любым фильмом (any(FilmDto.class)) возвращаем фильм с ID = 1
        when(filmStorage.create(any(FilmDto.class))).thenAnswer(invocation -> {
            // Получаем фильм, который передали в метод create
            FilmDto filmToCreate = invocation.getArgument(0);
            // Устанавливаем ID фильма равным 1
            filmToCreate.setId(1L);
            // Возвращаем фильм с установленным ID
            return filmToCreate;
        });

        // Выполняем POST-запрос на /films через MockMvc, чтобы создать новый фильм
        // Сохраняем ответ (JSON-строку) в переменную response
        String response = mockMvc.perform(post("/films")
                        // Указываем, что тело запроса — это JSON
                        .contentType(MediaType.APPLICATION_JSON)
                        // Преобразуем тестовый фильм в JSON-строку и отправляем в теле запроса
                        .content(objectMapper.writeValueAsString(film)))
                // Проверяем, что статус ответа — 200 (OK), то есть фильм успешно создан
                .andExpect(status().isOk())
                // Получаем содержимое ответа в виде строки
                .andReturn().getResponse().getContentAsString();

        // Преобразуем JSON-ответ (строку) обратно в объект FilmDto, чтобы получить созданный фильм
        FilmDto createdFilm = objectMapper.readValue(response, FilmDto.class);

        // Создаём новый объект FilmDto для обновления
        FilmDto updatedFilm = new FilmDto();
        // Устанавливаем ID обновляемого фильма равным ID созданного фильма
        updatedFilm.setId(createdFilm.getId());
        // Устанавливаем новое название фильма
        updatedFilm.setName("Updated Film");
        // Устанавливаем новое описание фильма
        updatedFilm.setDescription("Updated description");
        // Устанавливаем новую дату выпуска
        updatedFilm.setReleaseDate(LocalDate.of(2017, 10, 17));
        // Устанавливаем новую продолжительность
        updatedFilm.setDuration(150);
        // Устанавливаем MPA-рейтинг
        MpaRatingDto updatedMpa = new MpaRatingDto();
        updatedMpa.setId(4); // Соответствует "R"
        updatedMpa.setName("R");
        updatedFilm.setMpa(updatedMpa);
        // Устанавливаем жанры
        GenreDto updatedGenre = new GenreDto();
        updatedGenre.setId(2); // Соответствует "Драма"
        updatedGenre.setName("Драма");
        updatedFilm.setGenres(Collections.singletonList(updatedGenre));

        // Настраиваем мок filmStorage: при вызове findById с ID созданного фильма возвращаем Optional с созданным фильмом
        when(filmStorage.findById(createdFilm.getId())).thenReturn(Optional.of(createdFilm));
        // Настраиваем мок filmStorage: при вызове update с любым фильмом возвращаем обновлённый фильм (updatedFilm)
        when(filmStorage.update(any(FilmDto.class))).thenReturn(updatedFilm);

        // Выполняем PUT-запрос на /films через MockMvc, чтобы обновить фильм
        mockMvc.perform(put("/films")
                        // Указываем, что тело запроса — это JSON
                        .contentType(MediaType.APPLICATION_JSON)
                        // Преобразуем обновлённый фильм в JSON-строку и отправляем в теле запроса
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                // Проверяем, что в JSON-ответе поле name равно "Updated Film"
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.releaseDate").value("2017-10-17"))
                .andExpect(jsonPath("$.duration").value(150))
                // Проверяем, что MPA-рейтинг обновлён
                .andExpect(jsonPath("$.mpa.id").value(4))
                .andExpect(jsonPath("$.mpa.name").value("R"))
                // Проверяем, что жанры обновлены
                .andExpect(jsonPath("$.genres[0].id").value(2))
                .andExpect(jsonPath("$.genres[0].name").value("Драма"));
    }

    @Test // Проверяет, что можно получить список всех фильмов через GET-запрос
    void shouldGetAllFilms() throws Exception {
        // Настраиваем мок filmStorage: при вызове create с любым фильмом возвращаем фильм с ID = 1
        when(filmStorage.create(any(FilmDto.class))).thenAnswer(invocation -> {
            // Получаем фильм, который передали в метод create
            FilmDto filmToCreate = invocation.getArgument(0);
            // Устанавливаем ID фильма равным 1
            filmToCreate.setId(1L);
            return filmToCreate;
        });

        // Выполняем POST-запрос на /films через MockMvc, чтобы создать новый фильм
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());

        // Настраиваем мок filmStorage: при вызове findAll возвращаем список, содержащий наш тестовый фильм
        when(filmStorage.findAll()).thenReturn(List.of(film));

        // Выполняем GET-запрос на /films через MockMvc, чтобы получить список всех фильмов
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Film"))
                // Проверяем, что MPA-рейтинг возвращается корректно
                .andExpect(jsonPath("$[0].mpa.id").value(3))
                .andExpect(jsonPath("$[0].mpa.name").value("PG-13"))
                // Проверяем, что жанры возвращаются корректно
                .andExpect(jsonPath("$[0].genres[0].id").value(1))
                .andExpect(jsonPath("$[0].genres[0].name").value("Комедия"));
    }

    // Проверяет, что при создании фильма с пустым названием возвращается ошибка
    @Test
    void shouldFailWhenNameIsBlank() throws Exception {
        // Устанавливаем название фильма пустым
        film.setName("");
        // Выполняем POST-запрос на /films через MockMvc, чтобы создать фильм с пустым названием
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                // Проверяем, что статус ответа — 400 (Bad Request), так как валидация не прошла
                .andExpect(status().isBadRequest())
                // Проверяем, что в JSON-ответе поле error равно "Validation error"
                .andExpect(jsonPath("$.error").value("Validation error"))
                // Проверяем, что в JSON-ответе поле message равно "Название не может быть пустым"
                .andExpect(jsonPath("$.message").value("Название не может быть пустым"));
    }

    @Test // Проверяет, что при создании фильма с описанием длиннее 200 символов возвращается ошибка
    void shouldFailWhenDescriptionTooLong() throws Exception {
        film.setDescription("A".repeat(201));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Описание не может превышать 200 символов"));
    }

    @Test // Проверяет, что при создании фильма с датой релиза раньше 28 декабря 1895 года возвращается ошибка
    void shouldFailWhenReleaseDateTooEarly() throws Exception {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Дата релиза фильма не может быть раньше 28 декабря 1895г."));
    }

    @Test // Проверяет, что при создании фильма с нулевой или отрицательной продолжительностью возвращается ошибка
    void shouldFailWhenDurationNotPositive() throws Exception {
        film.setDuration(0);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test  // Проверяет, что при попытке обновить несуществующий фильм возвращается ошибка 404
    void shouldFailWhenUpdatingNonExistentFilm() throws Exception {
        film.setId(999L);
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Фильм с ID 999 не найден"));
    }

    @Test // Проверяет, что лайк можно успешно добавить через PUT-запрос
    void shouldAddLike() throws Exception {
        // Настраиваем мок filmStorage: при вызове findById с ID = 1 возвращаем Optional с нашим тестовым фильмом
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        // Настраиваем мок filmStorage: при вызове update с любым фильмом возвращаем переданный фильм
        when(filmStorage.update(any(FilmDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test  // Проверяет, что лайк можно успешно удалить через DELETE-запрос
    void shouldRemoveLike() throws Exception {
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(filmStorage.update(any(FilmDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());
    }

    @Test  // Проверяет, что можно получить список популярных фильмов через GET-запрос
    void shouldGetPopularFilms() throws Exception {
        // Создаём второй тестовый фильм (популярный, с двумя лайками)
        FilmDto film2 = new FilmDto();
        film2.setId(2L);
        film2.setName("Popular Film");
        film2.setDescription("A popular film");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(100);
        film2.setLikes(Set.of(1L, 2L));
        // Устанавливаем MPA-рейтинг для film2
        MpaRatingDto mpa2 = new MpaRatingDto();
        mpa2.setId(4); // Соответствует "R"
        mpa2.setName("R");
        film2.setMpa(mpa2);
        // Устанавливаем жанры для film2
        GenreDto genre2 = new GenreDto();
        genre2.setId(2); // Соответствует "Драма"
        genre2.setName("Драма");
        film2.setGenres(Collections.singletonList(genre2));

        // Настраиваем мок filmService: при вызове getPopularFilms с параметром 10 возвращаем список с
        // двумя фильмами (film2 и film)
        when(filmService.getPopularFilms(10)).thenReturn(List.of(film2, film));

        // Выполняем GET-запрос на /films/popular через MockMvc, чтобы получить список популярных фильмов
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Popular Film"))
                .andExpect(jsonPath("$[1].id").value(Matchers.nullValue())) // Используем Matchers.nullValue()
                .andExpect(jsonPath("$[1].name").value("Test Film"))
                // Проверяем, что MPA-рейтинг возвращается корректно
                .andExpect(jsonPath("$[0].mpa.id").value(4))
                .andExpect(jsonPath("$[0].mpa.name").value("R"))
                .andExpect(jsonPath("$[1].mpa.id").value(3))
                .andExpect(jsonPath("$[1].mpa.name").value("PG-13"))
                // Проверяем, что жанры возвращаются корректно
                .andExpect(jsonPath("$[0].genres[0].id").value(2))
                .andExpect(jsonPath("$[0].genres[0].name").value("Драма"))
                .andExpect(jsonPath("$[1].genres[0].id").value(1))
                .andExpect(jsonPath("$[1].genres[0].name").value("Комедия"));
    }
}