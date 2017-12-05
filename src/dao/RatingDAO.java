package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import factory.ConnectionFactory;
import model.Rating;

public class RatingDAO {
	private final Connection connection;
	
	public RatingDAO() {
		this.connection = new ConnectionFactory().getConnection();
	}
	
	public List<Rating> selectAllRating(){
		String sql = "SELECT * FROM ratings_norm";
		
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
		String sql = "SELECT * FROM ratings_norm where movieid = " + movieId;
		
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
		String sql = "SELECT * FROM ratings_norm where userid = " + userId;
		
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
		String sql = "SELECT * FROM ratings_norm where rating = " + rating;
		
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
		String sql = "select count(distinct userid) from ratings_norm";
		
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
	
	public void createTableReduction(){
		String sql = "CREATE TABLE ratings_new_user(userid bigint, movieid bigint, rating decimal, id bigint)";
		
		try {
			PreparedStatement st = connection.prepareStatement(sql);
			st.executeUpdate();
			st.close();
			
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
	}
	
	public void deleteTableReduction(){
		String sql = "DROP TABLE ratings_new_user";
		
		try {
			PreparedStatement st = connection.prepareStatement(sql);
			st.executeUpdate();
			st.close();
			
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
	}
	
	public void insertTableReduction(int user){
		int qtdRatingsUser = this.getQtdRatingsUser(user) / 2;
		String sql = "insert into ratings_new_user select * from ratings_norm where userid in(select userid from ratings_norm where movieid in (select movieid from ratings_norm where userid = " + user + ") group by userid having count(userid) > " + qtdRatingsUser + ")";
		
		try {
			PreparedStatement st = connection.prepareStatement(sql);
			st.executeUpdate();
			st.close();
			
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
	}
	
	public int getQtdRatingsUser(int user){
		String sql = "select count(userid) from ratings_norm where userid = " + user;
		
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
	
	public List<Rating> selectTableReduction(int user){		
		String sql = "select * from ratings_new_user";
		
		try {
			List<Rating> ratings = new ArrayList<>();
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
	
	public Map<Integer, List<Rating>> getQtdUsersReduction(){
		String sql = "select userid from ratings_new_user group by userid";
		
		try {
			Map<Integer, List<Rating>> userIds = new HashMap<>();
			PreparedStatement st = connection.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()){
				userIds.put(rs.getInt("userid"), new ArrayList<>());
			}
			
			rs.close();
			st.close();
			
			return userIds;
		} catch (Exception e) {
			System.out.println("[ERROR]: " + e.toString());
		}
		return null;
	}
}
