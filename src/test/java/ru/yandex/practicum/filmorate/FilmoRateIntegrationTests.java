package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для проверки работы хранилищ пользователей и фильмов через реальные компоненты Spring и базу данных.
 * Использует аннотации @JdbcTest и @ContextConfiguration для поднятия контекста только с нужными бинами.
 */
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {UserDbStorage.class, FilmDbStorage.class})
class FilmoRateIntegrationTests {
    // Хранилище пользователей
    private final UserDbStorage userStorage;
    // Хранилище фильмов
    private final FilmDbStorage filmStorage;
    // JdbcTemplate для прямых SQL-операций (например, очистки и инициализации данных)
    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод выполняется перед каждым тестом.
     * Очищает все таблицы и сбрасывает счетчики идентификаторов, затем инициализирует необходимые справочники (жанры и рейтинги).
     */
    @BeforeEach
    void setUp() {
        // Очистка таблиц в правильном порядке, учитывая зависимости
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM film_likes");
        jdbcTemplate.execute("DELETE FROM film_genre");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM genres");
        jdbcTemplate.execute("DELETE FROM mpa_ratings");

        // Сброс счётчиков идентификаторов
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1");

        // Инициализация справочника рейтингов MPA с явным mpa_id
        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, mpa_rating) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, mpa_rating) VALUES (2, 'PG')");
        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, mpa_rating) VALUES (3, 'PG-13')");
        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, mpa_rating) VALUES (4, 'R')");
        jdbcTemplate.update("INSERT INTO mpa_ratings (mpa_id, mpa_rating) VALUES (5, 'NC-17')");

        // Инициализация справочника жанров
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (3, 'Мультфильм')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (4, 'Триллер')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (5, 'Документальный')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (6, 'Боевик')");
    }

    /**
     * Проверяет создание и поиск пользователя по ID.
     */
    @Test
    void testFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.create(user);

        User finalUser = user;
        Optional<User> userOptional = userStorage.findById(user.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("id", finalUser.getId())
                                .hasFieldOrPropertyWithValue("email", "test@example.com")
                                .hasFieldOrPropertyWithValue("login", "testlogin")
                                .hasFieldOrPropertyWithValue("name", "Test User")
                                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1))
                );
    }

    /**
     * Проверяет создание нескольких пользователей и получение их списка.
     */
    @Test
    void testCreateAndFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1login");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1991, 2, 2));
        userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1992, 3, 3));
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(2)
                .anySatisfy(u -> assertThat(u).hasFieldOrPropertyWithValue("email", "user1@example.com"))
                .anySatisfy(u -> assertThat(u).hasFieldOrPropertyWithValue("email", "user2@example.com"));
    }

    /**
     * Проверяет обновление пользователя.
     */
    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@example.com");
        user.setLogin("updatelogin");
        user.setName("Update User");
        user.setBirthday(LocalDate.of(1993, 4, 4));
        user = userStorage.create(user);

        User finalUser = user;
        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setBirthday(LocalDate.of(1994, 5, 5));
        userStorage.update(updatedUser);

        Optional<User> updatedUserOptional = userStorage.findById(user.getId());

        assertThat(updatedUserOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("id", finalUser.getId())
                                .hasFieldOrPropertyWithValue("email", "updated@example.com")
                                .hasFieldOrPropertyWithValue("login", "newlogin")
                                .hasFieldOrPropertyWithValue("name", "New Name")
                                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1994, 5, 5))
                );
    }

    /**
     * Проверяет удаление пользователя.
     */
    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("deletelogin");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(1995, 6, 6));
        user = userStorage.create(user);

        User finalUser = user;
        userStorage.delete(user.getId());
        Optional<User> userOptional = userStorage.findById(user.getId());

        assertThat(userOptional).isEmpty();
    }

    /**
     * Проверяет создание и поиск фильма по ID.
     */
    @Test
    void testFindFilmById() {
        FilmDto film = new FilmDto();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRatingDto mpa = new MpaRatingDto();
        mpa.setId(3);
        mpa.setName("PG-13");
        film.setMpa(mpa);
        GenreDto genre = new GenreDto();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Collections.singletonList(genre));
        film = filmStorage.create(film);

        FilmDto finalFilm = film;
        Optional<FilmDto> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("id", finalFilm.getId())
                                .hasFieldOrPropertyWithValue("name", "Test Film")
                                .hasFieldOrPropertyWithValue("description", "A test film")
                                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2000, 1, 1))
                                .hasFieldOrPropertyWithValue("duration", 120)
                                .satisfies(filmDto -> assertThat(filmDto.getMpa().getId()).isEqualTo(3))
                                .satisfies(filmDto -> assertThat(filmDto.getGenres()).hasSize(1)
                                        .allSatisfy(g -> assertThat(g.getId()).isEqualTo(1)))
                );
    }

    /**
     * Проверяет создание нескольких фильмов и получение их списка.
     */
    @Test
    void testCreateAndFindAllFilms() {
        FilmDto film1 = new FilmDto();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2001, 2, 2));
        film1.setDuration(100);
        MpaRatingDto mpa1 = new MpaRatingDto();
        mpa1.setId(2);
        mpa1.setName("PG");
        film1.setMpa(mpa1);
        GenreDto genre1 = new GenreDto();
        genre1.setId(1);
        genre1.setName("Комедия");
        film1.setGenres(Collections.singletonList(genre1));
        filmStorage.create(film1);

        FilmDto film2 = new FilmDto();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2002, 3, 3));
        film2.setDuration(110);
        MpaRatingDto mpa2 = new MpaRatingDto();
        mpa2.setId(4);
        mpa2.setName("R");
        film2.setMpa(mpa2);
        GenreDto genre2 = new GenreDto();
        genre2.setId(2);
        genre2.setName("Драма");
        film2.setGenres(Collections.singletonList(genre2));
        filmStorage.create(film2);

        List<FilmDto> films = (List<FilmDto>) filmStorage.findAll();

        assertThat(films).hasSize(2)
                .anySatisfy(f -> assertThat(f)
                        .hasFieldOrPropertyWithValue("name", "Film 1")
                        .satisfies(filmDto -> assertThat(filmDto.getGenres()).hasSize(1)
                                .allSatisfy(g -> assertThat(g.getId()).isEqualTo(1))))
                .anySatisfy(f -> assertThat(f)
                        .hasFieldOrPropertyWithValue("name", "Film 2")
                        .satisfies(filmDto -> assertThat(filmDto.getGenres()).hasSize(1)
                                .allSatisfy(g -> assertThat(g.getId()).isEqualTo(2))));
    }

    /**
     * Проверяет обновление фильма.
     */
    @Test
    void testUpdateFilm() {
        FilmDto film = new FilmDto();
        film.setName("Original Film");
        film.setDescription("Original description");
        film.setReleaseDate(LocalDate.of(2003, 4, 4));
        film.setDuration(130);
        MpaRatingDto mpa = new MpaRatingDto();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);
        GenreDto genre = new GenreDto();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Collections.singletonList(genre));
        film = filmStorage.create(film);

        FilmDto finalFilm = film;
        FilmDto updatedFilm = new FilmDto();
        updatedFilm.setId(film.getId());
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2004, 5, 5));
        updatedFilm.setDuration(140);
        MpaRatingDto updatedMpa = new MpaRatingDto();
        updatedMpa.setId(3);
        updatedMpa.setName("PG-13");
        updatedFilm.setMpa(updatedMpa);
        GenreDto updatedGenre = new GenreDto();
        updatedGenre.setId(2);
        updatedGenre.setName("Драма");
        updatedFilm.setGenres(Collections.singletonList(updatedGenre));
        filmStorage.update(updatedFilm);

        Optional<FilmDto> updatedFilmOptional = filmStorage.findById(film.getId());

        assertThat(updatedFilmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("id", finalFilm.getId())
                                .hasFieldOrPropertyWithValue("name", "Updated Film")
                                .hasFieldOrPropertyWithValue("description", "Updated description")
                                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2004, 5, 5))
                                .hasFieldOrPropertyWithValue("duration", 140)
                                .satisfies(filmDto -> assertThat(filmDto.getMpa().getId()).isEqualTo(3))
                                .satisfies(filmDto -> assertThat(filmDto.getGenres()).hasSize(1)
                                        .allSatisfy(g -> assertThat(g.getId()).isEqualTo(2)))
                );
    }

    /**
     * Проверяет удаление фильма.
     */
    @Test
    void testDeleteFilm() {
        FilmDto film = new FilmDto();
        film.setName("Delete Film");
        film.setDescription("To be deleted");
        film.setReleaseDate(LocalDate.of(2005, 6, 6));
        film.setDuration(150);
        MpaRatingDto mpa = new MpaRatingDto();
        mpa.setId(4);
        mpa.setName("R");
        film.setMpa(mpa);
        GenreDto genre = new GenreDto();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Collections.singletonList(genre));
        film = filmStorage.create(film);

        FilmDto finalFilm = film;
        filmStorage.delete(film.getId());
        Optional<FilmDto> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional).isEmpty();
    }
}