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
    private Set<String> genres = new HashSet<>();
    private String MPA;

    public void putGenre(String genre) {
        genres.add(genre);
    }

    public void deleteGenre(String genre) {
        genres.remove(genre);
    }

    public void putLike(long id) {
        likesUsersId.add(id);
    }

    public void deleteLike(long id) {
        likesUsersId.remove(id);
    }

    public int getCount() {
        return likesUsersId.size();
    }
}
