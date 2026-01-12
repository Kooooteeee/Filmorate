package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film putLike(long filmId, long userId) {
        log.info("PUT /films/{}/like/{}", filmId, userId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        filmStorage.addLike(filmId, userId);
        return filmStorage.findById(filmId);
    }

    public Film deleteLike(long filmId, long userId) {
        log.info("DELETE /films/{}/like/{}", filmId, userId);

        filmStorage.findById(filmId);
        userStorage.findById(userId);

        filmStorage.deleteLike(filmId, userId);
        return filmStorage.findById(filmId);
    }

    public Collection<Film> getPopular(int count) {
        log.info("GET /films/popular?count={}", count);

        if (count < 1) {
            throw new ValidationException("Параметр count должен быть >= 1.");
        }

        return filmStorage.findPopular(count);
    }
}
