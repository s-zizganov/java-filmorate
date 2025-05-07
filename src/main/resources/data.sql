-- Инициализация жанров (соответствует enum Genre)
INSERT INTO genres (genre_id, genre_name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

-- Инициализация MPA-рейтингов
INSERT INTO mpa_ratings (mpa_rating) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');