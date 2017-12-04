package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dao.MovieDAO;
import dao.RatingDAO;
import model.Movie;
import model.Rating;
import model.Similarity;

public class Teste {
	
	public static void Teste1(){ // Mostrando todos os filmes
		MovieDAO moviedao = new MovieDAO();
		List<Movie> movies = moviedao.selectAllMovies();
		for(Movie m : movies){
			System.out.println("MovieId: " + m.getMovieId() + "; Title: " + m.getTitle());
		}
		System.out.println(movies.size());
	}
	
	public static void Teste2(){ // Mostrando todos os ratings
		RatingDAO ratingdao = new RatingDAO();
		List<Rating> ratings = ratingdao.selectAllRatingByRating(1);
		for(Rating r : ratings){
			System.out.println("Id: " + r.getId() + "; UserId: " + r.getUserId() + "; MovieId: " + r.getMovieId() + "; Rating: " + r.getRating());
		}
		System.out.println(ratings.size());
	}

	public static void Teste3(){ // QTD Movies / Users
		MovieDAO moviedao = new MovieDAO();
		System.out.println(moviedao.getQtdMovies());
		
		RatingDAO ratingdao = new RatingDAO();
		System.out.println(ratingdao.getQtdUsers());
	}
	
	public static void Teste4(){ // Jaccard e Cosseno Testes
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
	
	public static void Teste5(){ // Jaccard e Cosseno Testes no Banco
		RatingDAO ratingdao = new RatingDAO();
		int userIdA = 1;
		int userIdB = 3;
		
		System.out.println("Pegando do Banco os Ratings de A");
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B		
		System.out.println("Pegando do Banco os Ratings de B");
		List<Rating> ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
		
		Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);
		Recomendar recomendar = new Recomendar();
		recomendar.recomendarUsingAll(s);
		
		System.out.println("União: " + s.getUnion());
		System.out.println("-----");
		System.out.println("Intersecção: " + s.getIntersection());
		System.out.println("-----");
		System.out.println("Similaridade: " + s.getSimilarity());
		System.out.println("Distância de Jaccard: " + s.getDistanceJaccard());
		System.out.println("Distância de Cosseno: " + s.getDistanceCosseno());
	}
	
	public static void Teste6(){
		List<Similarity> rec = new ArrayList<>();
		RatingDAO ratingdao = new RatingDAO();
		int userIdA = 1;
		
		System.out.println("Pegando do Banco os Ratings de A");
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B
		
		Recomendar recomendacao = new Recomendar();
		List<Rating> ratingsB;
		for(int userIdB = 2; userIdB < 100; userIdB++){
//			System.out.println("Pegando do Banco os Ratings de B");
			//OTIMIZAÇÃO -> CRIAR UM MAP<IDUSUÁRIO,LISTA DE RATINGS>
			ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
			Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);
			
			s = recomendacao.recomendarUsingAll(s);
			
			if(s.getSimilarity() >= 0.1){
				rec.add(s);
			}
			
			System.out.println(userIdB);
		}
		exportarCSV("rec1", rec);
		
	}
	
	public static void exportarCSV(String fileName, List<Similarity> list){
		try{
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println("userIdA;userIdB;intersection;similarity;distanceJ;distanceCos;pearsonCorrelation");
			System.out.println("Exportando...");
			for (Similarity s : list) {
				writer.println(s.getUserIdA()+";"+s.getUserIdB()+";"+s.getIntersection()+";"+s.getSimilarity()+";"+s.getDistanceJaccard()+";"+s.getDistanceCosseno()+";"+s.getPearsonCorrelation());
			}
			System.out.println("Arquivo Exportado com sucesso!");
			
			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}
		
	public static void main(String[] args) {
		Teste6();
	}

}
