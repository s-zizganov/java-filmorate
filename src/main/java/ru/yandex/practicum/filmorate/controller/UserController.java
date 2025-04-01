package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс UserController отвечает за обработку HTTP-запросов, связанных с пользователями.
 */
@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    private void validateUser(User user) {
        // Проверка, что email не пустой.
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: Email пользователя не может быть пустым");
            throw new ValidationException("Email пользователя не может быть пустым");
        }
        // Проверка на наличие @.
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: Email должен содержать символ @");
            throw new ValidationException("Email должен содержать символ @");
        }

        // Проверка, что логин не пустой и не содержит пробелы.
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: Логин пользователя не может быть пустым");
            throw new ValidationException("Логин пользователя не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: Логин не может содержать пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        // Проверка, что дата рождения не в будущем.
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);

        // Проверяем, что email не используется другим пользователем
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            log.error("Ошибка: Email {} уже используется", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        user.setId(getNextId());

        // Если имя пустое, используем логин как имя
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        log.info("Добавлен новый пользователь с ID {}", user.getId());

        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        // Проверяем, указан ли ID
        if (user.getId() == null) {
            log.error("Ошибка валидации: ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        // Проверяем, существует ли пользователь
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        validateUser(user); // Проверяем данные пользователя

        User existingUser = users.get(user.getId()); // Получаем существующего пользовтеля

        // Проверяем, что новый email не занят другим пользователем
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                log.error("Ошибка: Email {} уже используется", user.getEmail());
                throw new DuplicatedDataException("Этот email уже используется");
            }
        }

        // Обновляем только те поля, которые переданы в запросе
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            existingUser.setLogin(user.getLogin());
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        } else if (user.getLogin() != null && (existingUser.getName() == null || existingUser.getName().isBlank())) {
            // Если имя пустое, а логин обновлён, используем новый логин как имя.
            existingUser.setName(user.getLogin());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }

        log.info("Пользователь с ID {} успешно обновлён", user.getId());
        return existingUser;
    }
}
