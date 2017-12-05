package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.RatingDAO;
import model.Rating;
import model.Similarity;

public class Prediction {

	Recomendar recomendacao;
	RatingDAO ratingdao;

	public Prediction() {
		recomendacao = new Recomendar();
		ratingdao = new RatingDAO();
	}

	public void predicao(int userIdA, String fileName){ // userId que eu quero fazer a predição
		//1. Reduzir a consulta
		List<Rating> ratings = preProcessamentoOtimizacao(userIdA);

		//Esse map estará com todos os ids dos usuários da tabela de otimizacao
		Map<Integer, List<Rating>> ratingsAll = this.ratingdao.getQtdUsersReduction();
		ratingsAll =  processamentoOtimizacao(ratings, ratingsAll);

		System.out.println("Quantidade de usuários: " + ratingsAll.size());

		//2. Pegar os ratings de A
		List<Rating> ratingsA = ratingsAll.get(userIdA);

		//3. Para cada usuário irei analisar a similaridade com o userIdA
		List<Similarity> similarities = new ArrayList<>();

		Set<Integer> allUsers = ratingsAll.keySet();
		allUsers.remove(userIdA);
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);

			s = this.recomendacao.recomendarUsingAll(s);

			if(s.getDistanceCosseno() >= 0.6 || s.getPearsonCorrelation() >= 0.6 || s.getPearsonCorrelation() <= -0.6){
				similarities.add(s);
			}
		}
		System.out.println("Finalizado os cálculos de similaridade!");
		
		// Exportar e excluir tabela de otimização
		exportCSV(fileName, similarities);
		this.ratingdao.deleteTableReduction();
	}

	public List<Rating> preProcessamentoOtimizacao(int userId){
		System.out.println("Organizando o banco... criando tabela para otimização");
		this.ratingdao.createTableReduction();
		System.out.println("Inserindo elementos para otimização...");
		this.ratingdao.insertTableReduction(userId);
		System.out.println("Recebendo ratings...");
		List<Rating> ratings = this.ratingdao.selectTableReduction(userId);
		return ratings;
	}

	public Map<Integer,List<Rating>> processamentoOtimizacao(List<Rating> ratings, Map<Integer,List<Rating>> ratingsAll){
		// Resumo: pegarei todos os ratings e irei colocar para cada rating em seu devido id (distribuição dos ratings nas listas dos userId dentro desse map)
		System.out.println("Organizando ratings...");
		for(Rating r : ratings){
			List<Rating> temp = ratingsAll.get(r.getUserId());
			temp.add(r);
			ratingsAll.put(r.getUserId(), temp);
		}
		
		System.out.println("Ratings dos usuários organizados!");

		return ratingsAll;
	}

	public void exportCSV(String fileName, List<Similarity> list){
		try{
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println("userIdA;userIdB;intersection;similarity;distanceJ;distanceCos;pearsonCorrelation");
			System.out.println("Exportando...");
			for (Similarity s : list) {
				writer.println(s.getUserIdA()+";"+s.getUserIdB()+";"+s.getIntersection()+";"+s.getSimilarity()+";"+s.getDistanceJaccard()+";"+s.getDistanceCosseno()+";"+s.getPearsonCorrelation());
			}
			System.out.println("Arquivo Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}

}
