package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of = "email")
@Getter
@Setter
@ToString
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Long> friendsId = new HashSet<>();
    private Set<Long> outgoingRequests = new HashSet<>();

    public void addFriend(long id) {
        friendsId.add(id);
    }

    public void removeFriend(long id) {
        friendsId.remove(id);
    }
}
