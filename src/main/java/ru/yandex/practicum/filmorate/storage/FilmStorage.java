package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAll();

    Film findById(long id);

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    Collection<Film> findPopular(int count);
}
