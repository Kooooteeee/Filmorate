package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private Mpa mpa;
    private Set<Genre> genres = new java.util.LinkedHashSet<>();


    private Integer likeCount;
}

