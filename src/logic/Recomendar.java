package logic;

import java.util.List;

import dao.RatingDAO;
import model.Rating;
import model.Similarity;

public class Recomendar {
	
	private Jaccard j;
	private Cosseno cos;
	private RatingDAO ratingdao;
	private int userIdA;
	private int userIdB;
	private Similarity s;
	
	public Recomendar(int userIdA, int userIdB) {
		j = new Jaccard();
		cos = new Cosseno();
		ratingdao = new RatingDAO();
		this.userIdA = userIdA;
		this.userIdB = userIdB;
		this.s = getRatingsAB(this.userIdA, this.userIdB);
	}
	
	public void recomendarUsingJaccard(){
		System.out.println("Iniciando Jaccard...");
		this.s = j.jaccard(s);
	}
	
	public void recomendarUsingCosseno(){
		System.out.println("Iniciando Cosseno...");
		this.s = cos.cosseno(s);
	}
	
	public Similarity recomendarUsingJaccardAndCosseno(){
		recomendarUsingJaccard();
		recomendarUsingCosseno();
		return this.s;
	}
	
	public Similarity getRatingsAB(int userIdA, int userIdB){
		System.out.println("Iniciando...");
		
		//I. Fazer a Matriz
		System.out.println("Pegando do Banco os Ratings de A");
		List<Rating> ratingsA = ratingdao.selectAllRatingByUserId(userIdA); // Aqui possuo os ratings de A e B
		System.out.println("Pegando do Banco os Ratings de B");
		List<Rating> ratingsB = ratingdao.selectAllRatingByUserId(userIdB);
		this.s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);
		return s;
	}

	public Similarity getS() {
		return s;
	}
	
}
