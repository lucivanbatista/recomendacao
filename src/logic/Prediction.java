package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

	public void iniciarPredicao(int userIdA, String fileName){ // userId que eu quero fazer a predição
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

		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usuários
		allUsers.remove(userIdA);
		
		//Observação: Aqui já iremos começar o processo para predição, iremos colocar nesse map os semelhantes de A
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);

			s = this.recomendacao.recomendarUsingAll(s);

			if(s.getDistanceCosseno() >= 0.6 || s.getPearsonCorrelation() >= 0.6 || s.getPearsonCorrelation() <= -0.6){
				similarities.add(s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predição
			}
		}
		System.out.println("Finalizado os cálculos de similaridade!");
		
		ratingsAll = null; // Garbage Collection irá funcionar
		ratings = null;
		System.gc();
		
		// Exportar e excluir tabela de otimização
		exportCSV(fileName, similarities);
		this.ratingdao.deleteTableReduction();
		
		Map<Integer, Integer> moviesReduction = reductionMovies(ratingsMini, ratingsA);
		predicao(moviesReduction, ratingsMini, similarities);
	}
	
	public Map<Integer, Integer> reductionMovies(Map<Integer,List<Rating>> ratingsMini, List<Rating> ratingsA){ // RECOMENDAR FILMES
		//Até agora, eu possuo um map com as chaves sendo os usuários semelhantes e A, e todos os seus filmes e seus ratings
		Map<Integer, Integer> movies = contarMovies(ratingsMini); // Contar os filmes para cálculos futuros (remover os menos assistidos)
		
		for(Rating r : ratingsA){ // Removendo os filmes que A já assistiu
			movies.remove(r.getMovieId());
		}
		
//		Map<Integer, Integer> moviesPosReduction = new HashMap<>();
//		
//		// ESSE FOR POSSIVELMENTE SERÁ RETIRADO E DEIXAREMOS APENAS OS MOVIES APÓS A CONTAGEM E A ELIMINAÇÃO DOS FILMES QUE A JÁ ASSISTIU
//		for(Integer movieId : movies.keySet()){ // Removendo os filmes que possuem do total de usuários similares um pouco acima da metade não assistiu
//			if(movies.get(movieId) > (ratingsMini.size() / 1.5)){
//				moviesPosReduction.put(movieId, movies.get(movieId));
//			}
//		}
//		movies = null;
		
		// A explicação é a seguinte, já que foi realizado um corte dos usuários para os que pelo menos assistiram a metade de filmes de userIdA
		// E depois foi realizado os cálculos de similaridade, buscando fazer com que os usuários mais similares fossem pegos
		// Os filmes desses usuários foram pegados, os que A já assistiu foram removidos e depois foram descartados os filmes que apenas alguns usuários
		// semelhantes assistiram, sobrando apenas os mais assistidos por eles
		// Agora é só realizar mais um cálculo da predição
		
//		System.out.println("Lista de Possíveis Filmes: ");
//		for(Integer movieId : moviesPosReduction.keySet()){ // For para mostrar os filmes e suas quantidades
//			System.out.println("Id: " + movieId + ", Quantidade: " + moviesPosReduction.get(movieId));
//		}
		System.out.println("----------------");
		System.out.println("Quantidade de Filmes Possíveis: " + movies.size());
		return movies;
//		return moviesPosReduction;
	}

	public void predicao(Map<Integer, Integer> moviesReduction, Map<Integer,List<Rating>> ratingsMini, List<Similarity> similarities){ //***ESTIMAR UMA NOTA QUE O USUÁRIO PODERÁ DAR***
		
	}
	
	public Map<Integer, Integer> contarMovies(Map<Integer,List<Rating>> ratingsMini){
		Map<Integer, Integer> movies = new HashMap<>(); // Lista com os filmes dos usuários semelhantes a A
		
		for(Integer userSimilar : ratingsMini.keySet()){ // Contagem dos filmes
			List<Rating> ratingsSimilar = ratingsMini.get(userSimilar);
			for(Rating r : ratingsSimilar){
				if(movies.containsKey(r.getMovieId())){
					movies.put(r.getMovieId(), movies.get(r.getMovieId()) + 1);
				}else{
					movies.put(r.getMovieId(), 1);
				}
			}
		}
		
		return movies;
	}
	
	public List<Rating> preProcessamentoOtimizacao(int userId){
		System.out.println("Organizando o banco... criando tabela para otimização");
		this.ratingdao.createTableReduction();
		System.out.println("Inserindo elementos na tabela para otimização...");
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
