package logic;

import java.util.List;

import model.Rating;
import model.Similarity;

public class Cosseno {
	
	public Cosseno() {
	}
	
	public Similarity cosseno(Similarity s){ // Usuário A e Usuário B		
		//II.
		//Distancia Cosseno = numerador / denominador
		double cosseno = numerator(s.getRatingsA(), s.getRatingsB()) / denominator(s.getRatingsA(), s.getRatingsB());
		s.setDistanceCosseno(cosseno);
		return s;
	}
	
	// Para cada filme que A e B avaliaram em comum, irei retornar a multiplicação das notas (Numerador)
	public Double numerator(List<Rating> ratingsA, List<Rating> ratingsB){
		double numerator = 0;
		for(Rating a : ratingsA){
			for(Rating b : ratingsB){
				if(a.getMovieId() == b.getMovieId()){
					numerator += (a.getRating() * b.getRating());
				}
			}
		}
		return numerator;
	}
	
	// Para cada lista de avaliações de um usuário, irei elevar e somar suas notas, depois retornar a raiz dessa soma
	public Double denominatorX(List<Rating> ratingsX){
		double contX = 0;
		for(Rating x : ratingsX){
			contX += Math.pow(x.getRating(), 2);
		}
		return Math.sqrt(contX);
	}
	
	public Double denominator(List<Rating> ratingsA, List<Rating> ratingsB){
		return denominatorX(ratingsA) * denominatorX(ratingsB);
	}
}
