package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dao.RatingDAO;
import model.Rating;

public class Cosseno {

	public RatingDAO ratingdao;
	
	public Cosseno() {
		ratingdao = new RatingDAO();
	}
	
	public Double cosseno(int userIdA, int userIdB){ // Usuário A e Usuário B
		//I. Fazer a matriz (mesma coisa que jaccard)
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B
		List<Rating> ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
		
		//II.
		//Distancia Cosseno = numerador / denominador
		double cosseno = numerator(ratingsA, ratingsB) / denominator(ratingsA, ratingsB);
		return cosseno;
	}
	
	// Para cada filme que A e B avaliaram em comum, irei retornar a multiplicação das notas (Numerador)
	public Double numerator(List<Rating> ratingsA, List<Rating> ratingsB){
		double numerator = 0;
		for(Rating a : ratingsA){
			for(Rating b : ratingsB){
				if(a.movieId == b.movieId){
					numerator += (a.rating * b.rating);
				}
			}
		}
		return numerator;
	}
	
	// Para cada lista de avaliações de um usuário, irei elevar e somar suas notas, depois retornar a raiz dessa soma
	public Double denominatorX(List<Rating> ratingsX){
		double contX = 0;
		for(Rating x : ratingsX){
			contX += Math.pow(x.rating, 2);
		}
		return Math.sqrt(contX);
	}
	
	public Double denominator(List<Rating> ratingsA, List<Rating> ratingsB){
		return denominatorX(ratingsA) * denominatorX(ratingsB);
	}
}
