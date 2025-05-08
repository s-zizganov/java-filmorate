package ru.yandex.practicum.filmorate.model;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Класс, представляющий модель дружбы между пользователями в приложении Filmorate.
 * Этот класс используется для хранения информации о дружеских отношениях между пользователями.
 * Каждый экземпляр класса представляет собой связь дружбы с определенным пользователем
 * и содержит информацию о статусе этой дружбы.
 */
// Аннотация @Data от Lombok генерирует геттеры, сеттеры, toString, equals и hashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Friendship {

    // Поле friendId — ID друга
    Long friendId;

    // Поле status — статус дружбы (неподтверждённая или подтверждённая)
    FriendshipStatus status;
}