package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Rating;

public class OperacoesConjunto {

	public OperacoesConjunto() {
		
	}
	
	// União de dois conjuntos A e B
	public Set<Integer> union(List<Rating> ratingsA, List<Rating> ratingsB){
		Set<Integer> unionAB = new HashSet<>();

		//Todos os elementos de A e Todos os elementos de B
		for(Rating a : ratingsA){
			unionAB.add(a.getMovieId());
		}
		for(Rating b : ratingsB){
			unionAB.add(b.getMovieId());
		}
		return unionAB;
	}

	// Intersecção de dois conjuntos A e B
	public Set<Integer> intersection(List<Rating> ratingsA, List<Rating> ratingsB){
		Set<Integer> intersectionAB = new HashSet<>();

		for(Rating a : ratingsA){
			for(Rating b : ratingsB){
				if(a.getMovieId() == b.getMovieId()){
					intersectionAB.add(a.getMovieId());
				}
			}
		}
		return intersectionAB;
	}
}
