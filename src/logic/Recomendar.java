package logic;

import model.Similarity;

public class Recomendar {
	
	private Jaccard j;
	private Cosseno cos;
	private Pearson p;
	private Similarity s;
	
	public Recomendar() {
		j = new Jaccard();
		cos = new Cosseno();
		p = new Pearson();
	}
	
	public void recomendarUsingJaccard(){
		this.s = j.jaccard(s);
	}
	
	public void recomendarUsingCosseno(){
		this.s = cos.cosseno(s);
	}
	
	public void recomendarUsingPearson(){
		this.s = p.pearson(s);
	}
	
	public Similarity recomendarUsingAll(Similarity s){
		this.s = s;
//		recomendarUsingJaccard();
//		recomendarUsingCosseno();
		recomendarUsingPearson();
		return this.s;
	}

	public Similarity getS() {
		return s;
	}	
}