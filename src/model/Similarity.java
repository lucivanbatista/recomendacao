package model;

import java.util.List;
import java.util.Set;

public class Similarity {

	private int userIdA;
	private int userIdB;
	private double similarityJaccard;
	private double distanceJaccard;
	private double distanceCosseno;
	private double pearsonCorrelation;
	private List<Rating> ratingsA;
	private List<Rating> ratingsB;
	
	//INICIO EXTRA
	
	private Set<Integer> union;
	private Set<Integer> intersection;
	
	//FIM EXTRA
	
	public Similarity(int userIdA, int userIdB) {
		this.userIdA = userIdA;
		this.userIdB = userIdB;
	}

	public Similarity(int userIdA, int userIdB, List<Rating> ratingsA, List<Rating> ratingsB) {
		this.userIdA = userIdA;
		this.userIdB = userIdB;
		this.ratingsA = ratingsA;
		this.ratingsB = ratingsB;
	}

	public Similarity(int userIdA, int userIdB, double similarity, double distanceJaccard) {
		this.userIdA = userIdA;
		this.userIdB = userIdB;
		this.similarityJaccard = similarity;
		this.distanceJaccard = distanceJaccard;
	}
	
	public Similarity(double similarity, double distanceJaccard) {
		this.similarityJaccard = similarity;
		this.distanceJaccard = distanceJaccard;
	}
	
	public Similarity(double distanceCosseno) {
		this.distanceCosseno = distanceCosseno;
	}

	public int getUserIdA() {
		return userIdA;
	}

	public int getUserIdB() {
		return userIdB;
	}

	public List<Rating> getRatingsA() {
		return ratingsA;
	}

	public List<Rating> getRatingsB() {
		return ratingsB;
	}

	public double getSimilarity() {
		return similarityJaccard;
	}

	public void setSimilarity(double similarity) {
		this.similarityJaccard = similarity;
	}

	public double getDistanceJaccard() {
		return distanceJaccard;
	}

	public void setDistanceJaccard(double distanceJaccard) {
		this.distanceJaccard = distanceJaccard;
	}

	public double getDistanceCosseno() {
		return distanceCosseno;
	}

	public void setDistanceCosseno(double distanceCosseno) {
		this.distanceCosseno = distanceCosseno;
	}

	public Set<Integer> getUnion() {
		return union;
	}

	public void setUnion(Set<Integer> union) {
		this.union = union;
	}

	public Set<Integer> getIntersection() {
		return intersection;
	}

	public void setIntersection(Set<Integer> intersection) {
		this.intersection = intersection;
	}

	public double getPearsonCorrelation() {
		return pearsonCorrelation;
	}

	public void setPearsonCorrelation(double pearsonCorrelation) {
		this.pearsonCorrelation = pearsonCorrelation;
	}
}
