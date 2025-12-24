package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film putLike(long id, long userId) {
        log.info("Получен запрос PUT /films/{id}/like/{userId}");
        if (filmStorage.findById(id) == null || userStorage.findById(userId) == null) {
            log.error("Фильм {} или/и пользователь {} не найдены",
                    filmStorage.findById(id),userStorage.findById(userId));
            throw new NotFoundException("Не найден фильм или пользовтаель.");
        }

        Film film = filmStorage.findById(id);
        User user = userStorage.findById(userId);

        film.putLike(userId);
        log.info("Лайк пользователя {} добавлен фильму {}.", user, film);
        return film;
    }

    public Film deleteLike(long id, long userId) {
        log.info("Получен запрос DELETE /films/{id}/like/{userId}");
        if (filmStorage.findById(id) == null || userStorage.findById(userId) == null) {
            log.error("Фильм {} или/и пользователь {} не найдены",
                    filmStorage.findById(id),userStorage.findById(userId));
            throw new NotFoundException("Не найден фильм или пользователь.");
        }

        Film film = filmStorage.findById(id);

        if(!film.getLikesUsersId().contains(userId)) {
            log.error("Пользователь {} не найден в списке поставивших лайк.", userStorage.findById(userId));
            throw new NotFoundException("Такой пользователь не ставил лайк этому фильму.");
        }

        film.deleteLike(userId);
        log.info("Лайк пользователя {} удален у фильма {}.", userStorage.findById(userId), film);
        return film;
    }

    public Collection<Film> getPopular(int count) {
        log.info("Получен запрос GET /films/popular?count={count}");

        if (count < 1) {
            log.error("Некорректный параметр count={}.", count);
            throw new ValidationException("Параметр count должен быть >= 1.");
        }

        int spCount = count;

        if (count > filmStorage.findAll().size()) {
            spCount = filmStorage.findAll().size();
        }

        List<Film> top = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getCount).reversed())
                .limit(spCount)
                .toList();

        log.info("Получен список самых популярных фильмов {}.", top);
        return top;
    }
}
