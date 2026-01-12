package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_SQL = "SELECT id, name FROM mpa ORDER BY id";

    private static final String FIND_BY_SQL = "SELECT id, name FROM mpa WHERE id = ?";

    private static final RowMapper<Mpa> MPA_MAPPER = (rs, rowNum) -> {
        Mpa m = new Mpa();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        return m;
    };

    @GetMapping
    public List<Mpa> findAll() {
        log.info("GET /mpa");
        return jdbcTemplate.query(FIND_ALL_SQL, MPA_MAPPER);
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        log.info("GET /mpa/{}", id);

        List<Mpa> found = jdbcTemplate.query(FIND_BY_SQL, MPA_MAPPER, id);
        if (found.isEmpty()) {
            throw new NotFoundException("MPA рейтинг не найден.");
        }
        return found.get(0);
    }
}
