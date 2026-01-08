# java-filmorate

Template repository for Filmorate project.



Ссылка на диаграмму
https://dbdiagram.io/d/695faa79d6e030a0247833d7



1\) Получить топ-N популярных фильмов (по лайкам)



SELECT

&nbsp; f.\*,

&nbsp; COUNT(fl.user\_id) AS likes\_count

FROM films AS f

LEFT JOIN film\_likes AS fl ON fl.film\_id = f.id

GROUP BY f.id

ORDER BY likes\_count DESC, f.id ASC

LIMIT N;



2\) Получить список жанров фильма



SELECT g.\*

FROM genres AS g

JOIN film\_genres AS fg ON fg.genre\_id = g.id

WHERE fg.film\_id = :filmId

ORDER BY g.id;



3\) Получить MPA-рейтинг фильма



SELECT m.\*

FROM mpa AS m

JOIN films AS f ON f.mpa\_id = m.id

WHERE f.id = :filmId;



4) Получить общих друзей двух пользователей (CONFIRMED)



SELECT u.\*

FROM users AS u

JOIN friendships AS fr1 ON fr1.friend\_id = u.id

JOIN friendships AS fr2 ON fr2.friend\_id = u.id

WHERE fr1.user\_id = :userId

&nbsp; AND fr2.user\_id = :otherId

&nbsp; AND fr1.status = 'CONFIRMED'

&nbsp; AND fr2.status = 'CONFIRMED'

ORDER BY u.id;





