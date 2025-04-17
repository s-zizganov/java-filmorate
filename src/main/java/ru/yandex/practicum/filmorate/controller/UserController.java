package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequestMapping("/users")
@RestController
public class UserController {

    // Константа BASE_PATH задаёт базовый путь для всех методов контроллера
    public static final String BASE_PATH = "/users";
    // Константа FRIEND_PATH задаёт путь для операций с друзьями (добавление и удаление)
    private static final String FRIEND_PATH = "/{id}/friends-{friendId}";
    // Константа COMMON_FRIENDS_PATH задаёт путь для получения общих друзей
    private static final String COMMON_FRIENDS_PATH = "/{id}/friends/common/{otherId}";

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    // Метод validateUser для проверки данных пользователя
    private void validateUser(User user) {
        log.debug("Начало валидации пользователя: {}", user);
        // Проверяем, что email пользователя не пустойб, не null и не состоит только из пробелов
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Ошибка валидации: Email пользователя не может быть пустым");
            throw new ValidationException("Email пользователя не может быть пустым");
        }
        // Проверяем, что email содержит символ @
        if (!user.getEmail().contains("@")) {
            log.error("Ошибка валидации: Email должен содержать символ @");
            throw new ValidationException("Email должен содержать символ @");
        }

        // Проверяем, что логин пользователя не пустой, не null и не состоит только из пробелов
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Ошибка валидации: Логин пользователя не может быть пустым");
            throw new ValidationException("Логин пользователя не может быть пустым");
        }
        // Проверяем, что логин не содержит пробелы
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: Логин не может содержать пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        // Проверяем, что дата рождения пользователя не в будущем (не позже текущей даты)
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация пользователя успешно завершена");
    }

    // Метод findAll для получения всех пользователей, обрабатывает GET-запрос на /users
    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        // Получаем всех пользователей из хранилища userStorage, метод findAll возвращает коллекцию пользователей
        Collection<User> users = userStorage.findAll();
        log.info("Возвращено {} пользователей", users.size());
        return users;
    }

    // Метод findById для получения пользователя по ID , обрабатывает GET-запрос на /users/id
    @GetMapping("/{id}")
    public User findById(@PathVariable("id") Long userId) {
        log.info("Получен запрос на получение пользователя с ID {}", userId);
        // Ищем пользователя в хранилище по ID, метод findById возвращает Optional<User>
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        log.debug("Найден пользователь: {}", user);
        return user;
    }

    // Метод create для создания нового пользователя, обрабатывает POST-запрос на /users
    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        validateUser(user);

        // Проверяем, что email пользователя ещё не используется
        if (userStorage.existsByEmail(user.getEmail())) {
            log.error("Ошибка: Email {} уже используется", user.getEmail());
            throw new DuplicatedDataException("Этот email уже используется");
        }

        // Если имя пользователя пустое или не указано, устанавливаем его равным логину
        user.setName(user.getName() == null || user.getName().isBlank() ? user.getLogin() : user.getName());
        log.debug("Имя пользователя установлено: {}", user.getName());
        // Сохраняем пользователя в хранилище, метод create возвращает созданного пользователя с установленным ID
        User addedUser = userStorage.create(user);
        log.info("Добавлен новый пользователь с ID {}", addedUser.getId());
        return addedUser;
    }

    // Метод updateUser для обновления существующего пользователя, обрабатывает PUT-запрос на /users
    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);
        // Проверяем, что ID пользователя указан
        if (user.getId() == null) {
            log.error("Ошибка валидации: ID пользователя должен быть указан");
            throw new ValidationException("ID пользователя должен быть указан");
        }

        // Ищем пользователя в хранилище по ID, чтобы убедиться, что он существует
        User existingUser = userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %d не найден", user.getId())));
        log.debug("Существующий пользователь: {}", existingUser);

        validateUser(user);

        // Если email изменён, проверяем, что новый email не используется другим пользователем
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userStorage.existsByEmail(user.getEmail())) {
                log.error("Ошибка: Email {} уже используется", user.getEmail());
                throw new DuplicatedDataException("Этот email уже используется");
            }
        }

        // Обновляем email, если он указан в запросе
        if (user.getEmail() != null) {
            log.debug("Обновление email: {}", user.getEmail());
            // Устанавливаем новый email для существующего пользователя
            existingUser.setEmail(user.getEmail());
        }
        // Обновляем логин, если он указан в запросе
        if (user.getLogin() != null) {
            log.debug("Обновление login: {}", user.getLogin());
            existingUser.setLogin(user.getLogin());
        }
        // Обновляем имя, если оно указано в запросе
        if (user.getName() != null) {
            log.debug("Обновление name: {}", user.getName());
            existingUser.setName(user.getName());
        } else if (user.getLogin() != null && (existingUser.getName() == null || existingUser.getName().isBlank())) {
            // Если имя не указано, но есть логин, и текущее имя пустое, устанавливаем имя равным логину
            log.debug("Установка name из login: {}", user.getLogin());
            existingUser.setName(user.getLogin());
        }
        // Обновляем дату рождения, если она указана в запросе
        if (user.getBirthday() != null) {
            log.debug("Обновление birthday: {}", user.getBirthday());
            existingUser.setBirthday(user.getBirthday());
        }

        // Обновляем пользователя в хранилище, метод update возвращает обновлённого пользователя
        User updatedUser = userStorage.update(existingUser);
        log.info("Пользователь с ID {} успешно обновлён", updatedUser.getId());
        return updatedUser;
    }

    // Метод addFriend для добавления друга, обрабатывает PUT-запрос на /users/id/friends/friendId
    @PutMapping(FRIEND_PATH)
    public void addFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление друга: userId={}, friendId={}", userId, friendId);
        userService.addFriend(userId, friendId);
        log.info("Друг успешно добавлен");
    }

    // Метод removeFriend для удаления друга обрабатывает DELETE-запрос на /users/id/frends/friendId
    @DeleteMapping(FRIEND_PATH)
    public void removeFriend(@PathVariable("id") Long userId, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление друга: userId={}, friendId={}", userId, friendId);
        userService.removeFriend(userId, friendId);
        log.info("Друг успешно удалён");
    }

    // Метод getFriends для получения списка друзей пользователя, обрабатывает GET-запрос на /users/id/friends
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") Long userId) {
        log.info("Получен запрос на получение списка друзей пользователя с ID {}", userId);
        // Вызываем метод getFriends в UserService, чтобы получить список друзей пользователя
        List<User> friends = userService.getFriends(userId);
        log.info("Возвращено {} друзей", friends.size());
        return friends;
    }

    // Метод getCommonFriends для получения списка общих друзей двух пользователей
    @GetMapping(COMMON_FRIENDS_PATH)
    public List<User> getCommonFriends(@PathVariable("id") Long userId, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей: userId={}, otherId={}", userId, otherId);
        List<User> commonFriends = userService.getCommonFriends(userId, otherId);
        log.info("Возвращено {} общих друзей", commonFriends.size());
        return commonFriends;
    }
}