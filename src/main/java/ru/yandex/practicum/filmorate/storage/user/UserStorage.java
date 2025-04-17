package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

// Интерфейс UserStorage определяет методы для работы с хранилищем пользователей
public interface UserStorage {

    User create(User user);

    User update(User user);

    void delete(Long id);

    // Метод findById для поиска пользователя по ID
    Optional<User> findById(Long id);

    // Метод findAll для получения всех пользователей
    Collection<User> findAll();

    // Метод для проверки, существует ли пользователь с указанным email
    boolean existsByEmail(String email);
}
