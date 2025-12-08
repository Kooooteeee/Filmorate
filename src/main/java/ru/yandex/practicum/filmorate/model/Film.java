package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(of = {"id"})
@Getter
@Setter
@ToString
public class Film {
    long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
}
