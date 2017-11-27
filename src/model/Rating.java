package model;

public class Rating {
	private int id;
	private int userId;
	private int movieId;
	private double rating;
	
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

	public int getId() {
		return id;
	}

	public int getUserId() {
		return userId;
	}

	public int getMovieId() {
		return movieId;
	}

	public double getRating() {
		return rating;
	}
}
