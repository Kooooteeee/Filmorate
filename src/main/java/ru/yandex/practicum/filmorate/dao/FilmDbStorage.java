package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.ResultSet;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final static String FIND_ALL_SQL = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id   AS mpa_id,
                    m.name AS mpa_name,
                    COUNT(fl.user_id) AS like_count
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY
                    f.id, f.name, f.description, f.release_date, f.duration,
                    m.id, m.name
                ORDER BY f.id
                """;

    private final static String FIND_BY_SQL = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id   AS mpa_id,
                    m.name AS mpa_name,
                    COUNT(fl.user_id) AS like_count
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                WHERE f.id = ?
                GROUP BY
                    f.id, f.name, f.description, f.release_date, f.duration,
                    m.id, m.name
                """;

    private final static String CREATE_SQL = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

    private final static String UPDATE_SQL = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;

    private final static String ADD_LIKE_SQL = """
                MERGE INTO film_likes (film_id, user_id)
                KEY (film_id, user_id)
                VALUES (?, ?)
                """;

    private final static String FIND_POP_SQL = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id   AS mpa_id,
                    m.name AS mpa_name,
                    COUNT(fl.user_id) AS like_count
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY
                    f.id, f.name, f.description, f.release_date, f.duration,
                    m.id, m.name
                ORDER BY like_count DESC, f.id ASC
                LIMIT ?
                """;

    private final static String LOAD_SQL = """
                SELECT g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

    private final static String DELETE_SQL= "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {

        List<Film> films = jdbcTemplate.query(FIND_ALL_SQL, (rs, rowNum) -> mapFilmBase(rs));

        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Film> filmById = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        loadGenresForFilms(filmById);

        return films;
    }

    @Override
    public Film findById(long id) {

        try {
            Film film = jdbcTemplate.queryForObject(FIND_BY_SQL, (rs, rowNum) -> mapFilmBase(rs), id);

            loadGenresForOneFilm(film);
            return film;
        } catch (NotFoundException e) {
            throw new NotFoundException("Фильм не найден.");
        }

    }

    @Override
    public Film create(Film film) {
        validateFilmForCreateOrUpdate(film);
        ensureMpaExists(extractMpaId(film));
        ensureGenresExist(film.getGenres());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, extractMpaId(film));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Не удалось получить id созданного фильма.");
        }

        long filmId = key.longValue();

        replaceFilmGenres(filmId, film.getGenres());

        return findById(filmId);
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Для обновления фильма нужен id.");
        }

        Film old = findById(newFilm.getId());

        if (newFilm.getName() == null) newFilm.setName(old.getName());
        if (newFilm.getDescription() == null) newFilm.setDescription(old.getDescription());
        if (newFilm.getReleaseDate() == null) newFilm.setReleaseDate(old.getReleaseDate());
        if (newFilm.getDuration() == null) newFilm.setDuration(old.getDuration());
        if (newFilm.getMpa() == null) newFilm.setMpa(old.getMpa());
        if (newFilm.getGenres() == null) newFilm.setGenres(old.getGenres());

        validateFilmForCreateOrUpdate(newFilm);
        ensureMpaExists(extractMpaId(newFilm));
        ensureGenresExist(newFilm.getGenres());

        jdbcTemplate.update(
                UPDATE_SQL,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                extractMpaId(newFilm),
                newFilm.getId()
        );

        replaceFilmGenres(newFilm.getId(), newFilm.getGenres());

        return findById(newFilm.getId());
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        jdbcTemplate.update(DELETE_SQL, filmId, userId);
    }

    @Override
    public Collection<Film> findPopular(int count) {

        List<Film> films = jdbcTemplate.query(FIND_POP_SQL, (rs, rowNum) -> mapFilmBase(rs), count);

        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Film> filmById = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        loadGenresForFilms(filmById);

        return films;
    }

    private Film mapFilmBase(ResultSet rs) throws java.sql.SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date release = rs.getDate("release_date");
        film.setReleaseDate(release != null ? release.toLocalDate() : null);

        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        film.setLikeCount(rs.getInt("like_count"));
        return film;
    }

    private void loadGenresForFilms(Map<Long, Film> filmById) {
        if (filmById.isEmpty()) return;

        String placeholders = filmById.keySet().stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                ORDER BY fg.film_id, g.id
                """, placeholders);

        Object[] args = filmById.keySet().toArray();

        jdbcTemplate.query(sql, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmById.get(filmId);
            if (film == null) return;

            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));

            film.getGenres().add(genre);
        }, args);
    }

    private void loadGenresForOneFilm(Film film) {

        List<Genre> genres = jdbcTemplate.query(LOAD_SQL, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            return genre;
        }, film.getId());

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void replaceFilmGenres(Long filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) {
            return;
        }

        String insert = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Genre> sorted = genres.stream()
                .filter(Objects::nonNull)
                .filter(g -> g.getId() != null)
                .sorted(Comparator.comparingInt(Genre::getId))
                .toList();

        jdbcTemplate.batchUpdate(insert, sorted, sorted.size(), (ps, genre) -> {
            ps.setLong(1, filmId);
            ps.setInt(2, genre.getId());
        });
    }

    private int extractMpaId(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("У фильма должен быть указан mpa (id).");
        }
        return film.getMpa().getId();
    }

    private void ensureMpaExists(int mpaId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa WHERE id = ?",
                Integer.class,
                mpaId
        );
        if (cnt == null || cnt == 0) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден.");
        }
    }

    private void ensureGenresExist(Set<Genre> genres) {
        if (genres == null) return;

        for (Genre g : genres) {
            if (g == null || g.getId() == null) continue;
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM genres WHERE id = ?",
                    Integer.class,
                    g.getId()
            );
            if (cnt == null || cnt == 0) {
                throw new NotFoundException("Жанр с id=" + g.getId() + " не найден.");
            }
        }
    }


    private void validateFilmForCreateOrUpdate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Пустое название.");
        }
        if (film.getDescription() == null) {
            throw new ValidationException("Описание не может быть null.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание должно быть короче 200 символов.");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("releaseDate не может быть null.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Некорректная дата релиза.");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Некорректная продолжительность.");
        }
        extractMpaId(film);
    }
}
