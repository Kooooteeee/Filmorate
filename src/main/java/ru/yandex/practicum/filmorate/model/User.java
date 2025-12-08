package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of = "email")
@Getter
@Setter
@ToString
public class User {
    long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
}
