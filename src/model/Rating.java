package model;

public class Rating {
	public int id;
	public int userId;
	public int movieId;
	public double rating;
	
	public Rating(int id, int userId, int movieId, double rating) {
		this.id = id;
		this.userId = userId;
		this.movieId = movieId;
		this.rating = rating;
	}
	
	public Rating(int userId, int movieId, double rating) {
		this.userId = userId;
		this.movieId = movieId;
		this.rating = rating;
	}
}
