package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.MovieDAO;
import dao.RatingDAO;
import model.Movie;
import model.Rating;
import model.Similarity;

public class Teste {
	
	public static void Teste1(){ // Mostrando todos os filmes
		MovieDAO moviedao = new MovieDAO();
		Map<Integer, Movie> movies = moviedao.selectAllMovies();
		for(Movie m : movies.values()){
			System.out.println("MovieId: " + m.getMovieId() + "; Title: " + m.getTitle());
		}
		System.out.println(movies.size());
	}
	
	public static void Teste2(){ // Mostrando todos os ratings
		RatingDAO ratingdao = new RatingDAO();
		List<Rating> ratings = ratingdao.selectAllRatingByRating(1);
		for(Rating r : ratings){
			System.out.println("UserId: " + r.getUserId() + "; MovieId: " + r.getMovieId() + "; Rating: " + r.getRating());
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
		Rating a = new Rating(1, 1, 4);
		Rating b = new Rating(1, 2, 1);
		Rating c = new Rating(1, 4, 5);
		Rating d = new Rating(1, 5, 4.5);
		
		Rating e = new Rating(2, 1, 2);
		Rating f = new Rating(2, 3, 3);
		Rating g = new Rating(2, 4, 5);
		
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
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A 
		
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
	
	public static void Teste7(){ // Pearson teste
		int idA = 1;
		int idB = 2;
		Rating a = new Rating(idA, 1, 4);
		Rating b = new Rating(idA, 2, 3);
		Rating c = new Rating(idA, 3, 5);
		
		Rating d = new Rating(idB, 1, 4.5);
		Rating e = new Rating(idB, 2, 3.5);
		
		List<Rating> ratingsA = new ArrayList<>();
		List<Rating> ratingsB = new ArrayList<>();
		
		ratingsA.add(a);
		ratingsA.add(b);
		ratingsA.add(c);
		ratingsB.add(d);
		ratingsB.add(e);
		
		Recomendar r = new Recomendar();
		Similarity s = new Similarity(idA, idB, ratingsA, ratingsB);
		r.recomendarUsingAll(s);
		
		System.out.println(s.getUnion());
		System.out.println("-----");
		System.out.println(s.getIntersection());
		System.out.println("-----");
		System.out.println("Similaridade = " + s.getSimilarity());
		System.out.println("-----");
		System.out.println("Cosseno = " + s.getDistanceCosseno());
		System.out.println("Pearson = " + s.getPearsonCorrelation());
		
		
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		ratingsMini.put(idA, ratingsA);
		ratingsMini.put(idB, ratingsB);
	}

	public static void Teste8(){
		Prediction p = new Prediction("user1");
		p.iniciarPredicao(1);
	}
	
	public static void Teste7MomentoNoiteDoida(){
//		Map<Integer, Integer> moviesReduction = new HashMap<>();
//		moviesReduction = p.reductionMovies(ratingsMini, ratingsB); // Esse map possui os possiveis filmes restantes após as exclusões
//		
//		List<Rating> finalRatings = new ArrayList<>(); // Esses ratings serão os que B não tem em comum com A e são possíveis filmes para A assistir e serão analisados
//		double finalSomatorioRatingsX = 0;
//		int cont = 0;
//		
//		for(Rating rating : ratingsMini.get(idA)){
//			if(moviesReduction.containsKey(rating.getMovieId())){ // Esse rating então será analisado
//				finalRatings.add(rating);
//			}else if(ratingsA.contains(rating)){
//				finalSomatorioRatingsX += rating.getRating();
//				cont++;
//			}
//		}
//		
//		double finalMediaRatingsX = finalSomatorioRatingsX / cont;
//		
//		double finalSomatorioRatingsUser = 0;
//		for(Rating rating : ratingsMini.get(idB)){
//			finalSomatorioRatingsUser += rating.getRating();
//		}
//		
//		double finalMediaUserRatings = finalSomatorioRatingsUser / ratingsMini.get(idB).size();
//		
//		double k = 0;
//		for(Rating rating : finalRatings){
//			k += (rating.getRating() - finalMediaRatingsX);
//		}
//		
//		double similarity = s.getPearsonCorrelation();
//		double prediction = finalMediaUserRatings + ((k * similarity) / similarity);
//		System.out.println(prediction);
	}
	
	public static void Teste9(){ // Testando Predição
		int id1 = 1; // Esse será o que iremos prever
		int id2 = 2;
		int id3 = 3;
		int id4 = 4;
		int id5 = 5;
		
		Rating a1 = new Rating(id1, 1, 3);
		Rating c1 = new Rating(id1, 3, 4);
		Rating d1 = new Rating(id1, 4, 3);
		Rating e1 = new Rating(id1, 5, 2);
		
		Rating a2 = new Rating(id2, 1, 4);
		Rating b2 = new Rating(id2, 2, 5);
		Rating c2 = new Rating(id2, 3, 4);
		Rating d2 = new Rating(id2, 4, 4);
		Rating e2 = new Rating(id2, 5, 2);
		
		Rating a3 = new Rating(id3, 1, 5);
		Rating b3 = new Rating(id3, 2, 1);
		Rating c3 = new Rating(id3, 3, 1);
		Rating d3 = new Rating(id3, 4, 5);
		Rating e3 = new Rating(id3, 5, 4);
		
		Rating a4 = new Rating(id4, 1, 4);
		Rating b4 = new Rating(id4, 2, 5);
		Rating c4 = new Rating(id4, 3, 3);
		Rating d4 = new Rating(id4, 4, 3);
		Rating e4 = new Rating(id4, 5, 3);
		
		Rating a5 = new Rating(id5, 1, 1);
		Rating b5 = new Rating(id5, 2, 3);
		Rating d5 = new Rating(id5, 4, 2);
		
		List<Rating> ratings1 = new ArrayList<>();
		List<Rating> ratings2 = new ArrayList<>();
		List<Rating> ratings3 = new ArrayList<>();
		List<Rating> ratings4 = new ArrayList<>();
		List<Rating> ratings5 = new ArrayList<>();
		
		ratings1.add(a1);
		ratings1.add(c1);
		ratings1.add(d1);
		ratings1.add(e1);
		
		ratings2.add(a2);
		ratings2.add(b2);
		ratings2.add(c2);
		ratings2.add(d2);
		ratings2.add(e2);
		
		ratings3.add(a3);
		ratings3.add(b3);
		ratings3.add(c3);
		ratings3.add(d3);
		ratings3.add(e3);
		
		ratings4.add(a4);
		ratings4.add(b4);
		ratings4.add(c4);
		ratings4.add(d4);
		ratings4.add(e4);
		
		ratings5.add(a5);
		ratings5.add(b5);
		ratings5.add(d5);
//		
//		Similarity s12 = new Similarity(id1, id2, ratings1, ratings2);
//		Similarity s13 = new Similarity(id1, id3, ratings1, ratings3);
//		Similarity s14 = new Similarity(id1, id4, ratings1, ratings4);
//		Similarity s15 = new Similarity(id1, id5, ratings1, ratings5);
		
		Recomendar r = new Recomendar();
//		s12 = r.recomendarUsingAll(s12);
//		s13 = r.recomendarUsingAll(s13);
//		s14 = r.recomendarUsingAll(s14);
//		s15 = r.recomendarUsingAll(s15);
//		
//		similaridades.add(s12);
//		similaridades.add(s13);
//		similaridades.add(s14);
//		similaridades.add(s15);
//		
//		System.out.println("Pearson12 = " + s12.getPearsonCorrelation());
//		System.out.println("Pearson13 = " + s13.getPearsonCorrelation());
//		System.out.println("Pearson14 = " + s14.getPearsonCorrelation());
//		System.out.println("Pearson15 = " + s15.getPearsonCorrelation());
		
		Prediction p = new Prediction("userTeste");

		Map<Integer, Similarity> similarities = new HashMap<>();
		
		Map<Integer, List<Rating>> ratingsAll = new HashMap<>();
		
		ratingsAll.put(1, ratings1);
		ratingsAll.put(2, ratings2);
		ratingsAll.put(3, ratings3);
		ratingsAll.put(4, ratings4);
		ratingsAll.put(5, ratings5);
		
		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usuários
		
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		allUsers.remove(id1);
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(id1, userIdB, ratings1, ratingsB);

			s = r.recomendarUsingAll(s);

			if(s.getPearsonCorrelation() >= 0.5 || s.getPearsonCorrelation() <= -0.5){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predição
			}
		}
		
		Map<Integer, Integer> moviesReduction = p.reductionMovies(ratingsMini, ratings1); // Filmes que A não possui
		
		double mean = 0;
		double ratingX = 0;
		double somatorioPredicao = 0;
		double somatorioSimilaridades = 0;
		double predicao = 0;
		double meanA = 0;
		
		for(Rating rating : ratings1){
			meanA += rating.getRating();
		}
		meanA = meanA / ratings1.size();
		
		for(Integer movie : moviesReduction.keySet()){
			for(Similarity s : similarities.values()){
				for(Rating rating : s.getRatingsB()){
					if(rating.getMovieId() != movie){
						mean += rating.getRating();
					}else if(rating.getMovieId() == movie){
						ratingX = rating.getRating();
					}
				}
				mean = mean / (s.getRatingsB().size() - 1);
				somatorioPredicao += (ratingX - mean) * s.getPearsonCorrelation();
				somatorioSimilaridades += Math.abs(s.getPearsonCorrelation());
				mean = 0;
				ratingX = 0;
				
			}
			System.out.println(somatorioPredicao);
			System.out.println(somatorioSimilaridades);
			predicao = meanA + (somatorioPredicao / somatorioSimilaridades);
			System.out.println(predicao);
		}
		
		
	}
	
	public static void Teste10(){ // Testando Prediçao com 6 pessoas FINAL
		int id1 = 1; // Esse será o que iremos prever
		int id2 = 2;
		int id3 = 3;
		int id4 = 4;
		int id5 = 5;
		int id6 = 6;
		
		Rating a1 = new Rating(id1, 1, 3);
		Rating c1 = new Rating(id1, 3, 4);
		Rating d1 = new Rating(id1, 4, 3);
		Rating e1 = new Rating(id1, 5, 2);
		
		Rating a2 = new Rating(id2, 1, 4);
		Rating b2 = new Rating(id2, 2, 5);
		Rating c2 = new Rating(id2, 3, 4);
		Rating d2 = new Rating(id2, 4, 4);
		Rating e2 = new Rating(id2, 5, 2);
		
		Rating a3 = new Rating(id3, 1, 5);
		Rating b3 = new Rating(id3, 2, 1);
		Rating c3 = new Rating(id3, 3, 1);
		Rating d3 = new Rating(id3, 4, 5);
		Rating e3 = new Rating(id3, 5, 4);
		
		Rating a4 = new Rating(id4, 1, 4);
		Rating b4 = new Rating(id4, 2, 5);
		Rating c4 = new Rating(id4, 3, 3);
		Rating d4 = new Rating(id4, 4, 3);
		Rating e4 = new Rating(id4, 5, 3);
		
		Rating a5 = new Rating(id5, 1, 1);
		Rating b5 = new Rating(id5, 2, 3);
		Rating d5 = new Rating(id5, 4, 2);
		
		Rating a6 = new Rating(id6, 1, 4);
		Rating b6 = new Rating(id6, 2, 1);
		Rating c6 = new Rating(id6, 3, 5);
		Rating d6 = new Rating(id6, 4, 4);
		Rating e6 = new Rating(id6, 5, 3);
		
		List<Rating> ratings1 = new ArrayList<>();
		List<Rating> ratings2 = new ArrayList<>();
		List<Rating> ratings3 = new ArrayList<>();
		List<Rating> ratings4 = new ArrayList<>();
		List<Rating> ratings5 = new ArrayList<>();
		List<Rating> ratings6 = new ArrayList<>();
		
		ratings1.add(a1);
		ratings1.add(c1);
		ratings1.add(d1);
		ratings1.add(e1);
		
		ratings2.add(a2);
		ratings2.add(b2);
		ratings2.add(c2);
		ratings2.add(d2);
		ratings2.add(e2);
		
		ratings3.add(a3);
		ratings3.add(b3);
		ratings3.add(c3);
		ratings3.add(d3);
		ratings3.add(e3);
		
		ratings4.add(a4);
		ratings4.add(b4);
		ratings4.add(c4);
		ratings4.add(d4);
		ratings4.add(e4);
		
		ratings5.add(a5);
		ratings5.add(b5);
		ratings5.add(d5);
		
		ratings6.add(a6);
		ratings6.add(b6);
		ratings6.add(c6);
		ratings6.add(d6);
		ratings6.add(e6);
		
		Recomendar r = new Recomendar();

		Prediction p = new Prediction("userTeste");

		Map<Integer, Similarity> similarities = new HashMap<>();
		
		Map<Integer, List<Rating>> ratingsAll = new HashMap<>();
		
		ratingsAll.put(1, ratings1);
		ratingsAll.put(2, ratings2);
		ratingsAll.put(3, ratings3);
		ratingsAll.put(4, ratings4);
		ratingsAll.put(5, ratings5);
		ratingsAll.put(6, ratings6);
		
		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usuários
		allUsers.remove(id1);
		
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(id1, userIdB, ratings1, ratingsB);

			s = r.recomendarUsingAll(s);

			if(s.getPearsonCorrelation() >= 0.5 || s.getPearsonCorrelation() <= -0.5){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predição
			}
		}
		System.out.println("Finalizado os cálculos de similaridade!");
		System.out.println("Quantidade de usuários similares: " + similarities.keySet().size());
		for(Similarity s : similarities.values()){
			System.out.println("User: " + s.getUserIdB() + "; Similaridade: " + s.getPearsonCorrelation());
		}
		p.finalPredicao(ratingsMini, ratings1, similarities);
		
	}
	
	public static void Teste11(){ // Testando Predicao com 5 pessoas FINAL
		int id1 = 1; // Esse será o que iremos prever
		int id2 = 2;
		int id3 = 3;
		int id4 = 4;
		int id5 = 5;
		
		Rating a1 = new Rating(id1, 1, 3);
		Rating c1 = new Rating(id1, 3, 4);
		Rating d1 = new Rating(id1, 4, 3);
		Rating e1 = new Rating(id1, 5, 2);
		
		Rating a2 = new Rating(id2, 1, 4);
		Rating b2 = new Rating(id2, 2, 5);
		Rating c2 = new Rating(id2, 3, 4);
		Rating d2 = new Rating(id2, 4, 4);
		Rating e2 = new Rating(id2, 5, 2);
		
		Rating a3 = new Rating(id3, 1, 5);
		Rating b3 = new Rating(id3, 2, 1);
		Rating c3 = new Rating(id3, 3, 1);
		Rating d3 = new Rating(id3, 4, 5);
		Rating e3 = new Rating(id3, 5, 4);
		
		Rating a4 = new Rating(id4, 1, 4);
		Rating b4 = new Rating(id4, 2, 5);
		Rating c4 = new Rating(id4, 3, 3);
		Rating d4 = new Rating(id4, 4, 3);
		Rating e4 = new Rating(id4, 5, 3);
		
		Rating a5 = new Rating(id5, 1, 1);
		Rating b5 = new Rating(id5, 2, 3);
		Rating d5 = new Rating(id5, 4, 2);
		
		List<Rating> ratings1 = new ArrayList<>();
		List<Rating> ratings2 = new ArrayList<>();
		List<Rating> ratings3 = new ArrayList<>();
		List<Rating> ratings4 = new ArrayList<>();
		List<Rating> ratings5 = new ArrayList<>();
		
		ratings1.add(a1);
		ratings1.add(c1);
		ratings1.add(d1);
		ratings1.add(e1);
		
		ratings2.add(a2);
		ratings2.add(b2);
		ratings2.add(c2);
		ratings2.add(d2);
		ratings2.add(e2);
		
		ratings3.add(a3);
		ratings3.add(b3);
		ratings3.add(c3);
		ratings3.add(d3);
		ratings3.add(e3);
		
		ratings4.add(a4);
		ratings4.add(b4);
		ratings4.add(c4);
		ratings4.add(d4);
		ratings4.add(e4);
		
		ratings5.add(a5);
		ratings5.add(b5);
		ratings5.add(d5);
		
		Recomendar r = new Recomendar();

		Prediction p = new Prediction("userTeste");

		Map<Integer, Similarity> similarities = new HashMap<>();
		
		Map<Integer, List<Rating>> ratingsAll = new HashMap<>();
		
		ratingsAll.put(1, ratings1);
		ratingsAll.put(2, ratings2);
		ratingsAll.put(3, ratings3);
		ratingsAll.put(4, ratings4);
		ratingsAll.put(5, ratings5);
		
		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usuários
		allUsers.remove(id1);
		
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(id1, userIdB, ratings1, ratingsB);

			s = r.recomendarUsingAll(s);

			if(s.getPearsonCorrelation() >= 0.5 || s.getPearsonCorrelation() <= -0.5){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predição
			}
		}
		System.out.println("Finalizado os cálculos de similaridade!");
		System.out.println("Quantidade de usuários similares: " + similarities.keySet().size());
		for(Similarity s : similarities.values()){
			System.out.println("User: " + s.getUserIdB() + "; Similaridade: " + s.getPearsonCorrelation());
		}
		p.finalPredicao(ratingsMini, ratings1, similarities);
		
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
		Teste11();
	}

}
