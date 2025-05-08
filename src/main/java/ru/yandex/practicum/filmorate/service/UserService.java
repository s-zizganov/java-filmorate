package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с бизнес-логикой, связанной с пользователями.
 * Позволяет добавлять и удалять друзей, получать список друзей и общих друзей.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    // Логгер для вывода информации в консоль и логи приложения
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // Хранилище пользователей, внедряется через конструктор (используется реализация userDbStorage)
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    /**
     * Добавить пользователя в друзья.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void addFriend(Long userId, Long friendId) {
        log.debug("Добавление друга: userId={}, friendId={}", userId, friendId);
        // Получаем пользователя и друга или выбрасываем исключение, если не найдены
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        // Проверяем, что друг ещё не добавлен
        boolean alreadyFriends = user.getFriends().stream()
                .anyMatch(f -> f.getFriendId().equals(friendId));
        if (alreadyFriends) {
            throw new ValidationException("Пользователь с ID " + friendId + " уже добавлен в друзья");
        }

        // Добавляем дружбу со статусом UNCONFIRMED и обновляем пользователя в хранилище
        Friendship friendship = new Friendship(friendId, FriendshipStatus.UNCONFIRMED);
        user.getFriends().add(friendship);
        userStorage.update(user);
        log.info("Добавлен друг с ID {} для пользователя с ID {}", friendId, userId);
    }

    /**
     * Удалить пользователя из друзей.
     * @param userId идентификатор пользователя
     * @param friendId идентификатор друга
     */
    public void removeFriend(Long userId, Long friendId) {
        log.debug("Удаление друга: userId={}, friendId={}", userId, friendId);
        // Получаем пользователя и друга или выбрасываем исключение, если не найдены
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        // Удаляем друга из списка друзей пользователя и обновляем пользователя в хранилище
        user.getFriends().removeIf(f -> f.getFriendId().equals(friendId));
        userStorage.update(user);
        log.info("Удалён друг с ID {} у пользователя с ID {}", friendId, userId);
    }

    /**
     * Получить список друзей пользователя.
     * @param userId идентификатор пользователя
     * @return список пользователей-друзей
     */
    public List<User> getFriends(Long userId) {
        log.debug("Получение списка друзей для пользователя с ID {}", userId);
        // Получаем пользователя или выбрасываем исключение, если не найден
        User user = getUserOrThrow(userId);

        // Преобразуем список Friendship в список User
        List<User> friends = user.getFriends().stream()
                .map(friendship -> getUserOrThrow(friendship.getFriendId()))
                .collect(Collectors.toList());

        log.debug("Найдено {} друзей", friends.size());
        return friends;
    }

    /**
     * Получить список общих друзей двух пользователей.
     * @param userId идентификатор первого пользователя
     * @param otherId идентификатор второго пользователя
     * @return список общих друзей
     */
    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Получение общих друзей для пользователей с ID {} и {}", userId, otherId);
        // Получаем обоих пользователей или выбрасываем исключение, если не найдены
        User user = getUserOrThrow(userId);
        User otherUser = getUserOrThrow(otherId);

        // Получаем списки идентификаторов друзей для обоих пользователей
        List<Long> userFriends = user.getFriends().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        List<Long> otherUserFriends = otherUser.getFriends().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());

        // Находим пересечение списков и возвращаем соответствующих пользователей
        List<User> commonFriends = userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(friendId -> getUserOrThrow(friendId))
                .collect(Collectors.toList());

        log.debug("Найдено {} общих друзей", commonFriends.size());
        return commonFriends;
    }

    /**
     * Вспомогательный метод: получить пользователя по ID или выбросить исключение, если не найден.
     * @param userId идентификатор пользователя
     * @return User
     */
    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}