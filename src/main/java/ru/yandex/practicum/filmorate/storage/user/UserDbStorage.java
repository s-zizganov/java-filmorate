package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Класс-реализация UserStorage для работы с пользователями через базу данных.
 * Позволяет создавать, обновлять, удалять, находить пользователей, а также управлять их друзьями.
 */
@Primary
@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(UserDbStorage.class);
    // JdbcTemplate для взаимодействия с базой данных
    private final JdbcTemplate jdbcTemplate;

    /**
     * Создание нового пользователя в базе данных.
     * @param user объект пользователя
     * @return созданный пользователь с присвоенным ID
     */
    @Override
    public User create(User user) {
        log.debug("Создание пользователя: {}", user);

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(userId);

        // Сохраняем друзей, если они есть
        if (!user.getFriends().isEmpty()) {
            saveFriends(user);
        }

        log.info("Пользователь с ID {} успешно создан", userId);
        return user;
    }

    /**
     * Обновление существующего пользователя в базе данных.
     * @param user объект пользователя с обновлёнными данными
     * @return обновлённый пользователь
     */
    @Override
    public User update(User user) {
        log.debug("Обновление пользователя: {}", user);

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int updatedRows = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        // Обновляем друзей: сначала удаляем старые записи, затем добавляем новые
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ?", user.getId());
        if (!user.getFriends().isEmpty()) {
            saveFriends(user);
        }

        log.info("Пользователь с ID {} успешно обновлён", user.getId());
        return user;
    }

    /**
     * Удаление пользователя по ID.
     * @param userId идентификатор пользователя
     */
    @Override
    public void delete(Long userId) {
        log.debug("Удаление пользователя с ID {}", userId);

        String sql = "DELETE FROM users WHERE user_id = ?";
        int deletedRows = jdbcTemplate.update(sql, userId);

        if (deletedRows == 0) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        log.info("Пользователь с ID {} успешно удалён", userId);
    }

    /**
     * Поиск пользователя по ID.
     * @param userId идентификатор пользователя
     * @return Optional с найденным пользователем или пустой, если не найден
     */
    @Override
    public Optional<User> findById(Long userId) {
        log.debug("Поиск пользователя с ID {}", userId);

        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, userId);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        // Загружаем друзей
        user.setFriends(loadFriends(userId));
        return Optional.of(user);
    }

    /**
     * Получение списка всех пользователей.
     * @return список всех пользователей
     */
    @Override
    public List<User> findAll() {
        log.debug("Получение списка всех пользователей");

        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);

        // Загружаем друзей для каждого пользователя
        for (User user : users) {
            user.setFriends(loadFriends(user.getId()));
        }

        return users;
    }

    /**
     * Проверка существования пользователя по email.
     * @param email электронная почта пользователя
     * @return true, если пользователь существует
     */
    @Override
    public boolean existsByEmail(String email) {
        log.debug("Проверка существования пользователя с email: {}", email);

        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Маппинг строки результата ResultSet в объект User.
     */
    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    /**
     * Загрузка списка друзей пользователя по его ID.
     */
    private List<Friendship> loadFriends(Long userId) {
        String sql = "SELECT followed_user_id, status FROM friends WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long friendId = rs.getLong("followed_user_id");
            FriendshipStatus status = FriendshipStatus.valueOf(rs.getString("status"));
            return new Friendship(friendId, status);
        }, userId);
    }

    /**
     * Сохранение друзей пользователя в базе данных.
     */
    private void saveFriends(User user) {
        String sql = "INSERT INTO friends (user_id, followed_user_id, status) VALUES (?, ?, ?)";
        user.getFriends().forEach(friendship -> jdbcTemplate.update(sql,
                user.getId(),
                friendship.getFriendId(),
                friendship.getStatus().name()
        ));
    }
}