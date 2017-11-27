package logic;

import java.util.List;
import java.util.Set;

import dao.RatingDAO;
import model.Rating;

public class Jaccard { // Similaridade Entre Usu�rios

	public RatingDAO ratingdao;
	public OperacoesConjunto opConjunto;
	
	public Jaccard() {
		ratingdao = new RatingDAO();
		opConjunto = new OperacoesConjunto();
	}
	
	//Futuramente, retirar essa consulta com o ratingdao desse local, j� que o Cosseno tamb�m utiliza
	public Double jaccard(int userIdA, int userIdB){ // Usu�rio A e Usu�rio B
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
	
	//Similaridade -> J(A,B) = M�dulo da interse��o de A e B / M�dulo da uni�o de A e B	
	public Double similarity(double union, double intersection){
		return Math.abs(intersection) / Math.abs(union);
	}
	
	public Double distanceJ(double similarity){
		return 1 - similarity;
	}
}
