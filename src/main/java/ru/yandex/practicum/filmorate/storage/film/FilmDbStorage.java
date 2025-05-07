package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.GenreDto;
import ru.yandex.practicum.filmorate.model.MpaRatingDto;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Реализация FilmStorage для работы с фильмами через базу данных.
 * Позволяет создавать, обновлять, удалять, находить фильмы, а также управлять жанрами и лайками.
 */
@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);
    // JdbcTemplate для взаимодействия с базой данных
    private final JdbcTemplate jdbcTemplate;

    /**
     * Создание нового фильма в базе данных.
     * @param film объект фильма
     * @return созданный фильм с присвоенным ID
     */
    @Override
    public FilmDto create(FilmDto film) {
        log.debug("Создание фильма: {}", film);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        // Сохраняем жанры
        saveGenres(film);
        // Сохраняем лайки
        saveLikes(film);

        log.info("Фильм с ID {} успешно создан", filmId);
        return film;
    }

    /**
     * Обновление существующего фильма в базе данных.
     * @param film объект фильма с обновлёнными данными
     * @return обновлённый фильм
     */
    @Override
    public FilmDto update(FilmDto film) {
        log.debug("Обновление фильма: {}", film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        // Обновляем жанры: удаляем старые и добавляем новые
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        saveGenres(film);

        // Обновляем лайки: удаляем старые и добавляем новые
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", film.getId());
        saveLikes(film);

        log.info("Фильм с ID {} успешно обновлён", film.getId());
        return film;
    }

    /**
     * Удаление фильма по ID.
     * @param filmId идентификатор фильма
     */
    @Override
    public void delete(Long filmId) {
        log.debug("Удаление фильма с ID {}", filmId);

        String sql = "DELETE FROM films WHERE film_id = ?";
        int deletedRows = jdbcTemplate.update(sql, filmId);

        if (deletedRows == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        log.info("Фильм с ID {} успешно удалён", filmId);
    }

    /**
     * Поиск фильма по ID.
     * @param filmId идентификатор фильма
     * @return Optional с найденным фильмом или пустой, если не найден
     */
    @Override
    public Optional<FilmDto> findById(Long filmId) {
        log.debug("Поиск фильма с ID {}", filmId);

        String sql = "SELECT f.*, m.mpa_rating FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id WHERE film_id = ?";
        List<FilmDto> films = jdbcTemplate.query(sql, this::mapRowToFilm, filmId);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        FilmDto film = films.get(0);
        // Загружаем жанры
        film.setGenres(loadGenres(filmId));
        // Загружаем лайки
        film.setLikes(new HashSet<>(loadLikes(filmId)));
        return Optional.of(film);
    }

    /**
     * Получение списка всех фильмов.
     * @return список всех фильмов
     */
    @Override
    public List<FilmDto> findAll() {
        log.debug("Получение списка всех фильмов");

        String sql = "SELECT f.*, m.mpa_rating FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        List<FilmDto> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        // Загружаем жанры и лайки для каждого фильма
        for (FilmDto film : films) {
            film.setGenres(loadGenres(film.getId()));
            film.setLikes(new HashSet<>(loadLikes(film.getId())));
        }

        return films;
    }

    /**
     * Маппинг строки результата ResultSet в объект FilmDto.
     */
    private FilmDto mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        FilmDto film = new FilmDto();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        MpaRatingDto mpa = new MpaRatingDto();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_rating"));
        film.setMpa(mpa);
        return film;
    }

    /**
     * Загрузка списка жанров для фильма по его ID.
     */
    private List<GenreDto> loadGenres(Long filmId) {
        String sql = "SELECT g.genre_id, g.genre_name FROM film_genre fg JOIN genres g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            GenreDto genre = new GenreDto();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            return genre;
        }, filmId);
    }

    /**
     * Загрузка списка лайков (ID пользователей) для фильма по его ID.
     */
    private List<Long> loadLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, filmId);
    }

    /**
     * Сохранение жанров фильма в базе данных.
     */
    private void saveGenres(FilmDto film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> jdbcTemplate.update(sql, film.getId(), genre.getId()));
        }
    }

    /**
     * Сохранение лайков фильма в базе данных.
     */
    private void saveLikes(FilmDto film) {
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            film.getLikes().forEach(userId -> jdbcTemplate.update(sql, film.getId(), userId));
        }
    }
}