package logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.Rating;
import model.Similarity;

public class Pearson {

	public OperacoesConjunto opConjunto;
	
	Map<Integer, Rating> ratingsA;
	Map<Integer, Rating> ratingsB;

	public Pearson() {
		opConjunto = new OperacoesConjunto();
		this.ratingsA = new HashMap<>();
		this.ratingsB = new HashMap<>();
	}

	public Similarity pearson(Similarity s){		

		Set<Integer> intersection = opConjunto.intersection(s.getRatingsA(), s.getRatingsB());

		s.setIntersection(intersection);
		preProcessamentoPearson(s);
		
		double meanA = getMediaRatings(this.ratingsA); // m�dia ratings A
		double meanB = getMediaRatingsB(this.ratingsB, intersection); // m�dia ratings B

		s.setPearsonCorrelation(functionPearson(intersection, meanA, meanB));
		return s;
	}

	public Double functionPearson(Set<Integer> intersection, double meanA, double meanB){
		double count = 0; //(rating A no item i - m�dia ratings A) * (rating B no item i - m�dia ratings B)
		double countPowA = 0; // Somat�rio (rating A no item i - m�dia ratings A)^2
		double countPowB = 0; // Somat�rio (rating B no item i - m�dia ratings B)^2
		
		//double count2 = 0; //sqrt(somat�rio (rating A no item i - m�dia ratings A)^2) * somat�rio((rating B no item i - m�dia ratings B)^2)
		
		for(Integer movie : intersection){
			double a = this.ratingsA.get(movie).getRating();
			double b = this.ratingsB.get(movie).getRating();
//			System.out.println("Para o movie n� " + movie + ", Rating A: " + ratingsA.get(movie).getRating() + ", Rating B: " + ratingsB.get(movie).getRating());
			
			count += (a - meanA) *  (b - meanB);
			countPowA += Math.pow((a - meanA), 2);
			countPowB += Math.pow((b - meanB), 2);
		}
		
		if((count > -0.00000001 && count <= 0.00000001) || ((countPowA * countPowB) > -0.00000001) && ((countPowA * countPowB) <= 0.00000001)){
			return 0.0;
		}
		
//		System.out.println("Count: " + count + "; PowA: " + countPowA + "; PowB: " + countPowB);
//		System.out.println((Math.sqrt(countPowA * countPowB)));
//		System.out.println(count / (Math.sqrt(countPowA * countPowB)));
		return (count / (Math.sqrt(countPowA * countPowB)));		
	}

	public void preProcessamentoPearson(Similarity s){		
		for(Rating a : s.getRatingsA()){
			this.ratingsA.put(a.getMovieId(), a);
		}
		
		for(Rating b : s.getRatingsB()){
			this.ratingsB.put(b.getMovieId(), b);
		}
	}
	
	public double getMediaRatings(Map<Integer, Rating> ratings){
		double mean = 0;
		for(Rating r : ratings.values()){
			mean += r.getRating();
		}
		mean = mean / ratings.keySet().size();
		return mean;
	}
	
	public double getMediaRatingsB(Map<Integer, Rating> ratings, Set<Integer> intersection){ // Ele � diferente, pois o c�lculo � baseado nos que eles possuem iguais
		double mean = 0;
		for(Integer movie : intersection){
			mean += ratings.get(movie).getRating();
		}
		mean = mean / intersection.size();
		return mean;
	}
	
}