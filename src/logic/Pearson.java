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
		
		double meanA = getMediaRatings(this.ratingsA); // média ratings A
		double meanB = getMediaRatings(this.ratingsB); // média ratings B
		
		s.setPearsonCorrelation(functionPearson(intersection, meanA, meanB));
		return s;
	}

	public Double functionPearson(Set<Integer> intersection, double meanA, double meanB){
		double count = 0; //(rating A no item i - média ratings A) * (rating B no item i - média ratings B)
		double countPowA = 0; // Somatório (rating A no item i - média ratings A)^2
		double countPowB = 0; // Somatório (rating B no item i - média ratings B)^2
		
		//double count2 = 0; //sqrt(somatório (rating A no item i - média ratings A)^2) * somatório((rating B no item i - média ratings B)^2)
		
		for(Integer movie : intersection){
			Rating a = this.ratingsA.get(movie);
			Rating b = this.ratingsB.get(movie);
			
			count += (a.getRating() - meanA) *  (b.getRating() - meanB);
			countPowA += Math.pow((a.getRating() - meanA), 2);
			countPowB += Math.pow((b.getRating() - meanB), 2);
		}
		
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
}