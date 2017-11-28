package logic;

import model.Similarity;

public class Recomendar {
	
	private Jaccard j;
	private Cosseno cos;
//	private RatingDAO ratingdao;
	private Similarity s;
	
//	public Recomendar(int userIdA, int userIdB) {
//		j = new Jaccard();
//		cos = new Cosseno();
//		ratingdao = new RatingDAO();
//		this.userIdA = userIdA;
//		this.userIdB = userIdB;
//		this.s = getRatingsAB(this.userIdA, this.userIdB);
//	}
	
	public Recomendar(/*Similarity s*/) {
		j = new Jaccard();
		cos = new Cosseno();
//		ratingdao = new RatingDAO();
//		this.s = s;
	}
	
	public void recomendarUsingJaccard(){
		this.s = j.jaccard(s);
	}
	
	public void recomendarUsingCosseno(){
		this.s = cos.cosseno(s);
	}
	
	public Similarity recomendarUsingJaccardAndCosseno(Similarity s){
		this.s = s;
		recomendarUsingJaccard();
		recomendarUsingCosseno();
		return this.s;
	}
	
//	public Similarity getRatingsAB(int userIdA, int userIdB){
//		System.out.println("Iniciando...");
//		
//		//I. Fazer a Matriz
//		System.out.println("Pegando do Banco os Ratings de A");
//		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B
//		System.out.println("Pegando do Banco os Ratings de B");
//		List<Rating> ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
//		this.s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);
//		return s;
//	}

	public Similarity getS() {
		return s;
	}	
}