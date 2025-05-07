package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * DAO (Data Access Object) для работы с жанрами фильмов через базу данных.
 * Позволяет получать список всех жанров и искать жанр по идентификатору.
 */
@Component
@RequiredArgsConstructor
public class GenreDao {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(GenreDao.class);
    // JdbcTemplate для взаимодействия с базой данных
    private final JdbcTemplate jdbcTemplate;

    // Маппинг genre_id на Genre
    private static final Map<Integer, Genre> ID_TO_GENRE = Map.of(
            1, Genre.COMEDY,
            2, Genre.DRAMA,
            3, Genre.ANIMATION,
            4, Genre.THRILLER,
            5, Genre.DOCUMENTARY,
            6, Genre.ACTION
    );

    /**
     * Получить список всех жанров из базы данных.
     * @return список жанров
     */
    public List<Genre> findAll() {
        log.debug("Получение списка всех жанров");
        String sql = "SELECT genre_id, genre_name FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre);
        log.info("Найдено {} жанров", genres.size());
        return genres;
    }

    /**
     * Найти жанр по идентификатору.
     * @param id идентификатор жанра
     * @return найденный жанр
     * @throws NotFoundException если жанр не найден
     */
    public Genre findById(Integer id) {
        log.debug("Поиск жанра с ID {}", id);
        String sql = "SELECT genre_id, genre_name FROM genres WHERE genre_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        log.info("Найден жанр с ID {}", id);
        return genres.get(0);
    }

    /**
     * Маппинг строки результата ResultSet в объект Genre.
     */
    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("genre_id");
        Genre genre = ID_TO_GENRE.get(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        return genre;
    }
}