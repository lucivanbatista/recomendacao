package model;

public class Rating implements Comparable<Rating> {
	private int userId;
	private int movieId;
	private double rating;

	public Rating(int userId, int movieId, double rating) {
		this.userId = userId;
		this.movieId = movieId;
		this.rating = rating;
	}

	public Rating(int movieId, double rating) {
		this.movieId = movieId;
		this.rating = rating;
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

	@Override
	public int compareTo(Rating o) {
		if (this.rating > o.getRating()) {
			return -1;
		}
		if (this.rating < o.getRating()) {
			return 1;
		}
		return 0;
	}
}
