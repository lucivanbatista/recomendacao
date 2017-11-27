package factory;


import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.SQLException;

public class ConnectionFactory {

	public Connection getConnection(){
		try{
			Class.forName("org.postgresql.Driver");
			return DriverManager.getConnection("jdbc:postgresql://localhost:5432/movies", "postgres", "postgres");
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Teste de conexão
	//	public static void main(String[] args) throws SQLException {
	//		Connection conexao = DriverManager.getConnection("jdbc:postgresql://localhost:5432/db_data_taxi_tdrive","postgres", "postgres");
	//		System.out.println("Conectado!");
	//		conexao.close();
	//	}
}