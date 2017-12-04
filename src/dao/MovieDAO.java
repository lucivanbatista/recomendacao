package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Movie;

public class MovieDAO {

	private final Connection connection;
	
	public MovieDAO(){
		this.connection = new ConnectionFactory().getConnection();
	}
	
	public List<Movie> selectAllMovies(){
		String sql = "SELECT * FROM movies";
		
		try {
			ArrayList<Movie> movies = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				Movie m = new Movie(rs.getInt("movieid"), rs.getString("title"));
				movies.add(m);
			}
			
			rs.close();
			st.close();
			
			return movies;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}
	
	public int getQtdMovies(){
		String sql = "select count(*) from movies";
		
		try {
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			int qtd = 0;
			while(rs.next()){
				qtd = rs.getInt("count");
			}
			
			rs.close();
			st.close();
			
			return qtd;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return 0;
	}
}
