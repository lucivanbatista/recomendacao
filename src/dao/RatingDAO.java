package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Rating;

public class RatingDAO {
	private final Connection connection;
	
	public RatingDAO() {
		this.connection = new ConnectionFactory().getConnection();
	}
	
	public List<Rating> selectAllRating(){
		String sql = "SELECT * FROM ratings";
		
		try {
			ArrayList<Rating> ratings = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			System.out.println("OK");
			while(rs.next()){
				Rating r = new Rating(rs.getInt("id"), rs.getInt("userid"), rs.getInt("movieid"), rs.getDouble("rating"));
				ratings.add(r);
			}
			
			rs.close();
			st.close();
			
			return ratings;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}

	public List<Rating> selectAllRatingByMovieId(int movieId){
		String sql = "SELECT * FROM ratings where movieid = " + movieId;
		
		try {
			ArrayList<Rating> ratings = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				Rating r = new Rating(rs.getInt("id"), rs.getInt("userid"), rs.getInt("movieid"), rs.getDouble("rating"));
				ratings.add(r);
			}
			
			rs.close();
			st.close();
			
			return ratings;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}
	
	public List<Rating> selectAllRatingByUserId(int userId){
		String sql = "SELECT * FROM ratings where userid = " + userId;
		
		try {
			ArrayList<Rating> ratings = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				Rating r = new Rating(rs.getInt("id"), rs.getInt("userid"), rs.getInt("movieid"), rs.getDouble("rating"));
				ratings.add(r);
			}
			
			rs.close();
			st.close();
			
			return ratings;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}
	
	public List<Rating> selectAllRatingByRating(double rating){
		String sql = "SELECT * FROM ratings where rating = " + rating;
		
		try {
			ArrayList<Rating> ratings = new ArrayList<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				Rating r = new Rating(rs.getInt("id"), rs.getInt("userid"), rs.getInt("movieid"), rs.getDouble("rating"));
				ratings.add(r);
			}
			
			rs.close();
			st.close();
			
			return ratings;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}
	
	public int getQtdUsers(){
		String sql = "select count(distinct userid) from ratings";
		
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
