package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO (Data Access Object) для работы с рейтингами MPA через базу данных.
 * Позволяет получать список всех рейтингов и искать рейтинг по идентификатору.
 */
@Component
@RequiredArgsConstructor
public class MpaRatingDao {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(MpaRatingDao.class);
    // JdbcTemplate для взаимодействия с базой данных
    private final JdbcTemplate jdbcTemplate;

    /**
     * Получить список всех рейтингов MPA из базы данных.
     * @return список рейтингов MPA
     */
    public List<MpaRating> findAll() {
        log.debug("Получение списка всех рейтингов MPA");
        String sql = "SELECT mpa_id, mpa_rating FROM mpa_ratings ORDER BY mpa_id";
        List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpaRating);
        log.info("Найдено {} рейтингов MPA", ratings.size());
        return ratings;
    }

    /**
     * Найти рейтинг MPA по идентификатору.
     * @param id идентификатор рейтинга
     * @return найденный рейтинг MPA
     * @throws NotFoundException если рейтинг не найден
     */
    public MpaRating findById(Integer id) {
        log.debug("Поиск рейтинга MPA с ID {}", id);
        String sql = "SELECT mpa_id, mpa_rating FROM mpa_ratings WHERE mpa_id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpaRating, id);
        if (ratings.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
        }
        log.info("Найден рейтинг MPA с ID {}", id);
        return ratings.get(0);
    }

    /**
     * Маппинг строки результата ResultSet в объект MpaRating.
     */
    private MpaRating mapRowToMpaRating(ResultSet rs, int rowNum) throws SQLException {
        String rating = rs.getString("mpa_rating");
        return MpaRating.valueOf(rating.replace("-", "_"));
    }
}