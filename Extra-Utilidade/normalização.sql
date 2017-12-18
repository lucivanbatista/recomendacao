CREATE or REPLACE FUNCTION normalizar() RETURNS void AS $$
DECLARE
	cursor1 CURSOR is select userid, movieid,rating,id from ratings2;
	cont_id int; -- Contador para o loop
	userid ratings2.userid%TYPE;
	movieid ratings2.movieid%TYPE;
	rating real;
	id ratings2.id%TYPE;
BEGIN
	OPEN cursor1; -- Cursor para o loop
	FETCH cursor1 into userid, movieid, rating, id;
	LOOP
		EXIT WHEN NOT FOUND;
		INSERT into ratings_norm values (userid, movieid,rating * 0.2,id);
		FETCH cursor1 into userid, movieid, rating, id; -- Pula para o próximo no cursor
	END LOOP;
	CLOSE cursor1;
END;
$$ language plpgsql;


select normalizar2();

--Executar primeiro
CREATE TABLE ratings_norm
(
  userid bigint,
  movieid bigint,
  rating decimal,
  id serial NOT NULL
)

CREATE TABLE ratings_new
(
  userid bigint,
  movieid bigint,
  rating decimal
)

insert into ratings_new select userid, movieid, cast(rating as DECIMAL) from ratings_new2;




CREATE or REPLACE FUNCTION normalizar() RETURNS void AS $$
DECLARE
	cursor1 CURSOR is select userid, movieid, rating from ratings_new;
	cont_id int; -- Contador para o loop
	userid ratings_new.userid%TYPE;
	movieid ratings_new.movieid%TYPE;
	rating ratings_new.rating%TYPE;
BEGIN
	OPEN cursor1; -- Cursor para o loop
	FETCH cursor1 into userid, movieid, rating;
	LOOP
		EXIT WHEN NOT FOUND;
		INSERT into ratings_norm values (userid, movieid,rating * 0.2);
		FETCH cursor1 into userid, movieid, rating; -- Pula para o próximo no cursor
	END LOOP;
	CLOSE cursor1;
END;
$$ language plpgsql;


select normalizar3();



--Usuários que possuem os filmes de 1
insert into ratings_user_1
select * from ratings_norm where userid in(
	select userid from ratings_norm where movieid in 
		(select movieid from ratings_norm where userid = 1) group by userid having count(userid) > 12);





