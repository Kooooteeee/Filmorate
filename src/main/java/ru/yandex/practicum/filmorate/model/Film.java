package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(of = {"id"})
@Getter
@Setter
@ToString
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likesUsersId = new HashSet<>();
    private int count = 0;

    public void putLike(long id) {
        if (likesUsersId.add(id)) {
            count++;
        }
    }

    public void deleteLike(long id) {
        if (likesUsersId.remove(id)) {
            count--;
        }
    }
}
