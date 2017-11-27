package logic;

import java.util.List;
import java.util.Set;

import dao.RatingDAO;
import model.Rating;

public class Jaccard { // Similaridade Entre Usuários

	public RatingDAO ratingdao;
	public OperacoesConjunto opConjunto;
	
	public Jaccard() {
		ratingdao = new RatingDAO();
		opConjunto = new OperacoesConjunto();
	}
	
	//Futuramente, retirar essa consulta com o ratingdao desse local, já que o Cosseno também utiliza
	public Double jaccard(int userIdA, int userIdB){ // Usuário A e Usuário B
		//I. Fazer a matriz
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B
		List<Rating> ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
		
		//II. Calcular a Similaridade / Distancia de Jaccard
		//DistanciaJ = 1 - J(A,B)
		Set<Integer> union = opConjunto.union(ratingsA, ratingsB);
		Set<Integer> intersection = opConjunto.intersection(ratingsA, ratingsB);
		
		double similarity = similarity(union.size(), intersection.size());
		return similarity;
	}
	
	//Similaridade -> J(A,B) = Módulo da interseção de A e B / Módulo da união de A e B	
	public Double similarity(double union, double intersection){
		return Math.abs(intersection) / Math.abs(union);
	}
	
	public Double distanceJ(double similarity){
		return 1 - similarity;
	}
}
