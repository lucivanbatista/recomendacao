package model;

public class Movie {
	public int movieId;
	public String title;
	public String genres;
	
	public Movie(int movieId, String title, String genres) {
		this.movieId = movieId;
		this.title = title;
		this.genres = genres;
	}
	
	public Movie(int movieId, String title) {
		this.movieId = movieId;
		this.title = title;
	}
}
