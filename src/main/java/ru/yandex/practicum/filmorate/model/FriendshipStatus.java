package ru.yandex.practicum.filmorate.model;


// Перечисление FriendshipStatus представляет статус дружбы между двумя пользователями
public enum FriendshipStatus {
    UNCONFIRMED("неподтверждённая"), // Один пользователь отправил запрос, другой ещё не подтвердил
    CONFIRMED("подтверждённая");     // Оба пользователя подтвердили дружбу

    // Поле для хранения названия статуса на русском языке
    private final String status;

    // Конструктор перечисления, принимает название статуса
    FriendshipStatus(String status) {
        this.status = status;
    }

    // Геттер для получения названия статуса
    public String getStatus() {
        return status;
    }

    // Переопределяем метод toString, чтобы возвращать название статуса
    @Override
    public String toString() {
        return status;
    }
}