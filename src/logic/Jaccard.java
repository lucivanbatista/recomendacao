package logic;

import java.util.Set;

import model.Similarity;

public class Jaccard { // Similaridade Entre Usu�rios

	public OperacoesConjunto opConjunto;
	
	public Jaccard() {
		opConjunto = new OperacoesConjunto();
	}
	
	//Futuramente, retirar essa consulta com o ratingdao desse local, j� que o Cosseno tamb�m utiliza
	public Similarity jaccard(Similarity s){ // Usu�rio A e Usu�rio B		
		//II. Calcular a Similaridade / Distancia de Jaccard
		//DistanciaJ = 1 - J(A,B)
		Set<Integer> union = opConjunto.union(s.getRatingsA(), s.getRatingsB());
		Set<Integer> intersection = opConjunto.intersection(s.getRatingsA(), s.getRatingsB());
		
		//INICIO EXTRA
		s.setUnion(union);
		s.setIntersection(intersection);
		//FIM EXTRA
		
		double similarity = similarity(union.size(), intersection.size());
		double distanceJ = distanceJ(similarity);
		
		s.setSimilarity(similarity);
		s.setDistanceJaccard(distanceJ);
		return s;
	}
	
	//Similaridade -> J(A,B) = M�dulo da interse��o de A e B / M�dulo da uni�o de A e B	
	public Double similarity(double union, double intersection){
		return Math.abs(intersection) / Math.abs(union);
	}
	
	public Double distanceJ(double similarity){
		return 1 - similarity;
	}
}
