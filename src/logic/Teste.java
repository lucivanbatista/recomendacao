package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dao.MovieDAO;
import dao.RatingDAO;
import model.Movie;
import model.Rating;

public class Teste {
	
	public static void Teste1(){ // Mostrando todos os filmes
		MovieDAO moviedao = new MovieDAO();
		List<Movie> movies = moviedao.selectAllMovies();
		for(Movie m : movies){
			System.out.println("MovieId: " + m.movieId + "; Title: " + m.title);
		}
		System.out.println(movies.size());
	}
	
	public static void Teste2(){ // Mostrando todos os ratings
		RatingDAO ratingdao = new RatingDAO();
		List<Rating> ratings = ratingdao.selectAllRatingByRating(1);
		for(Rating r : ratings){
			System.out.println("Id: " + r.id + "; UserId: " + r.userId + "; MovieId: " + r.movieId + "; Rating: " + r.rating);
		}
		System.out.println(ratings.size());
	}

	public static void Teste3(){ // QTD Movies / Users
		MovieDAO moviedao = new MovieDAO();
		System.out.println(moviedao.getQtdMovies());
		
		RatingDAO ratingdao = new RatingDAO();
		System.out.println(ratingdao.getQtdUsers());
	}
	
	public static void Teste4(){
		Rating a = new Rating(1, 1, 1, 4);
		Rating b = new Rating(2, 1, 2, 1);
		Rating c = new Rating(3, 1, 4, 5);
		Rating d = new Rating(4, 1, 5, 4.5);
		
		Rating e = new Rating(5, 2, 1, 2);
		Rating f = new Rating(6, 2, 3, 3);
		Rating g = new Rating(7, 2, 4, 5);
		
		List<Rating> ratingsA = new ArrayList<>();
		List<Rating> ratingsB = new ArrayList<>();
		
		ratingsA.add(a);
		ratingsA.add(b);
		ratingsA.add(c);
		ratingsA.add(d);
		ratingsB.add(e);
		ratingsB.add(f);
		ratingsB.add(g);
		
		Jaccard j = new Jaccard();
		OperacoesConjunto opConjunto = new OperacoesConjunto();
		Set<Integer> uniao = j.opConjunto.union(ratingsA, ratingsB);
		Set<Integer> interseccao = j.opConjunto.intersection(ratingsA, ratingsB);
		
		Cosseno cos = new Cosseno();
		
		System.out.println(uniao);
		System.out.println("-----");
		System.out.println(interseccao);
		System.out.println("-----");
		System.out.println("Similaridade = " + j.similarity(uniao.size(), interseccao.size()));
		System.out.println("-----");
		System.out.println("Cosseno = " + cos.numerator(ratingsA, ratingsB) / cos.denominator(ratingsA, ratingsB));
	}
	
	public static void main(String[] args) {
		Teste4();
	}

}
