package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateAndFindById_withMpaAndGenres() {
        Film film = createFilm("film1", 1, List.of(1, 2));
        Film created = filmStorage.create(film);

        Film found = filmStorage.findById(created.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("film1");
        assertThat(found.getMpa()).isNotNull();
        assertThat(found.getMpa().getId()).isEqualTo(1);
        assertThat(found.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmStorage.create(createFilm("old", 1, List.of(1)));

        Film upd = new Film();
        upd.setId(created.getId());
        upd.setName("new");
        upd.setDescription("new desc");
        upd.setReleaseDate(LocalDate.of(2001, 1, 1));
        upd.setDuration(111);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        upd.setMpa(mpa);

        upd.setGenres(new HashSet<>(List.of(genre(3))));

        Film updated = filmStorage.update(upd);

        assertThat(updated.getName()).isEqualTo("new");
        assertThat(updated.getMpa().getId()).isEqualTo(2);
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(3);
    }

    @Test
    void shouldAddAndDeleteLike_andPopular() {

        var u1 = userStorage.create(TestData.user("u1@mail.com", "u1"));
        var u2 = userStorage.create(TestData.user("u2@mail.com", "u2"));

        Film f1 = filmStorage.create(createFilm("f1", 1, List.of()));
        Film f2 = filmStorage.create(createFilm("f2", 1, List.of()));

        filmStorage.addLike(f1.getId(), u1.getId());
        filmStorage.addLike(f1.getId(), u2.getId());
        filmStorage.addLike(f2.getId(), u1.getId());

        List<Film> popular = (List<Film>) filmStorage.findPopular(10);

        assertThat(popular).extracting(Film::getId).contains(f1.getId(), f2.getId());
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId());

        filmStorage.deleteLike(f1.getId(), u2.getId());
        Film refreshed = filmStorage.findById(f1.getId());
        assertThat(refreshed.getLikeCount()).isEqualTo(1);
    }

    private Film createFilm(String name, int mpaId, List<Integer> genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(mpaId);
        film.setMpa(mpa);

        var set = new HashSet<Genre>();
        for (Integer gid : genreIds) {
            set.add(genre(gid));
        }
        film.setGenres(set);

        return film;
    }

    private Genre genre(int id) {
        Genre g = new Genre();
        g.setId(id);
        return g;
    }

    private static class TestData {
        static ru.yandex.practicum.filmorate.model.User user(String email, String login) {
            ru.yandex.practicum.filmorate.model.User u = new ru.yandex.practicum.filmorate.model.User();
            u.setEmail(email);
            u.setLogin(login);
            u.setName(login);
            u.setBirthday(LocalDate.of(2000, 1, 1));
            return u;
        }
    }
}
