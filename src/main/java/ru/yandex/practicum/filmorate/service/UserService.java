package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service // Аннотация @Service указывает, что этот класс является сервисом в Spring (содержит бизнес-логику)
public class UserService {

    // Переменная userStorage для работы с хранилищем пользователей
    private final UserStorage userStorage;

    // Конструктор класса UserService, который принимает зависимость userStorage
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    // Метод getUserOrThrow для получения пользователя по Id или выброса исключения, если пользователь не найден
    private User getUserOrThrow(Long userId) {
        log.debug("Поиск пользователя с ID {}", userId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        log.debug("Пользователь найден: {}", user);
        return user;
    }

    // Метод addFriend для добавления друга пользователю
    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга: userId={}, friendId={}", userId, friendId);
        // Получаем пользователя по ID, используя метод getUserOrThrow
        User user = getUserOrThrow(userId);
        // Получаем друга по ID
        User friend = getUserOrThrow(friendId);

        // Добавляем ID друга в список друзей пользователя
        user.getFriends().add(friendId);
        // Добавляем ID пользователя в список друзей друга (дружба взаимная)
        friend.getFriends().add(userId);
        // Обновляем пользователя в хранилище
        userStorage.update(user);
        // Обновляем друга в хранилище
        userStorage.update(friend);
        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
    }

    // Метод removeFriend для удаления друга у пользователя
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друга: userId={}, friendId={}", userId, friendId);
        // Получаем пользователя по ID
        User user = getUserOrThrow(userId);
        // Получаем друга по ID
        User friend = getUserOrThrow(friendId);

        // Удаляем ID друга из списка друзей пользователя
        user.getFriends().remove(friendId);
        // Удаляем ID пользователя из списка друзей друга (дружба взаимная)
        friend.getFriends().remove(userId);
        // Обновляем пользователя в хранилище
        userStorage.update(user);
        // Обновляем друга в хранилище
        userStorage.update(friend);
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    // Метод getFriends для получения списка друзей пользователя
    public List<User> getFriends(Long userId) {
        log.debug("Получение списка друзей для пользователя с ID {}", userId);
        // Получаем пользователя по ID
        User user = getUserOrThrow(userId);
        // Получаем список друзей пользователя: берём список ID друзей, преобразуем каждый ID в объект User
        // и собираем в список
        List<User> friends = user.getFriends().stream()
                // Для каждого ID друга вызываем метод getUserOrThrow, чтобы получить объект User
                .map(this::getUserOrThrow)
                // Преобразуем Stream в List
                .collect(Collectors.toList());
        log.debug("Найдено {} друзей", friends.size());
        return friends;
    }

    // Метод getCommonFriends для получения списка общих друзей двух пользователей
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        log.debug("Получение общих друзей: userId={}, otherUserId={}", userId, otherUserId);
        // Получаем первого пользователя по ID
        User user = getUserOrThrow(userId);
        // Получаем второго пользователя по ID
        User otherUser = getUserOrThrow(otherUserId);

        // Создаём коллекцию commonFriends, копируя список друзей первого пользователя
        Set<Long> commonFriends = new HashSet<>(user.getFriends());
        log.debug("Друзья пользователя {}: {}", userId, user.getFriends());
        log.debug("Друзья пользователя {}: {}", otherUserId, otherUser.getFriends());
        // Оставляем в commonFriends те ID, которые есть в списке друзей второго пользователя (находим пересечение)
        commonFriends.retainAll(otherUser.getFriends());
        log.debug("Общие друзья: {}", commonFriends);

        // Преобразуем множество ID общих друзей в список объектов User
        List<User> result = commonFriends.stream()
                // Для каждого ID друга вызываем метод getUserOrThrow, чтобы получить объект User
                .map(this::getUserOrThrow)
                // Преобразуем Stream в List
                .collect(Collectors.toList());
        log.debug("Найдено {} общих друзей", result.size());
        return result;
    }
}