package logic;

import java.io.PrintWriter;
import java.util.Collection;
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

		System.out.println("Quantidade de usuários possíveis similares: " + ratingsAll.size());

		//2. Pegar os ratings de A
		List<Rating> ratingsA = ratingsAll.get(userIdA);
		
		//3. Para cada usuário irei analisar a similaridade com o userIdA
		Map<Integer, Similarity> similarities = new HashMap<>(); // Id do usuário semelhante ao A e Similarity

		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usuários
		allUsers.remove(userIdA);
		
		//Observação: Aqui já iremos começar o processo para predição, iremos colocar nesse map os semelhantes de A
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		
		System.out.println("Iniciando cálculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);

			s = this.recomendacao.recomendarUsingAll(s);

			if(s.getPearsonCorrelation() >= 0.6 || s.getPearsonCorrelation() <= -0.6){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predição
			}
		}
		System.out.println("Finalizado os cálculos de similaridade!");
		
		ratingsAll = null; // Garbage Collection irá funcionar
		ratings = null;
		System.gc();
		
		// Exportar e excluir tabela de otimização
		exportSimilaritiesCSV(fileName, similarities.values());
		this.ratingdao.deleteTableReduction();
		System.out.println("Quantidade de usuários similares: " + similarities.keySet().size());
		
		Map<Integer, Integer> moviesReduction = reductionMovies(ratingsMini, ratingsA); // Filmes que A não possui
		Map<Integer, Double> predicoes = predicao(moviesReduction, ratingsMini, similarities, ratingsA);
		
		exportPredictionsCSV(fileName, predicoes);
		
		for(Integer movie : moviesReduction.keySet()){
			System.out.println("Id: " + movie + "; Qtd: " + moviesReduction.get(movie));
		}
//		for(Integer movie : predicoes.keySet()){
//			System.out.println("Id do Movie: " + predicoes.get(movie));
//		}
	}
	
	public Map<Integer, Integer> reductionMovies(Map<Integer,List<Rating>> ratingsMini, List<Rating> ratingsA){ // RECOMENDAR FILMES
		//Até agora, eu possuo um map com as chaves sendo os usuários semelhantes e A, e todos os seus filmes e seus ratings
		Map<Integer, Integer> movies = contarMovies(ratingsMini); // Contar os filmes para cálculos futuros (remover os menos assistidos)
		
		for(Rating r : ratingsA){ // Removendo os filmes que A já assistiu
			movies.remove(r.getMovieId());
		}
		
		System.out.println("Quantidade de Filmes Possíveis PRÉ REDUÇÃO: " + movies.size());
		
		Map<Integer, Integer> moviesPosReduction = new HashMap<>();
		
		// ESSE FOR POSSIVELMENTE SERÁ RETIRADO E DEIXAREMOS APENAS OS MOVIES APÓS A CONTAGEM E A ELIMINAÇÃO DOS FILMES QUE A JÁ ASSISTIU
		for(Integer movieId : movies.keySet()){ // Removendo os filmes que possuem do total de usuários similares a metade não assistiu
			if(movies.get(movieId) > (ratingsMini.size() / 2)){
				moviesPosReduction.put(movieId, movies.get(movieId));
			}
		}
		movies = null;
		
		// A explicação é a seguinte, já que foi realizado um corte dos usuários para os que pelo menos assistiram a metade de filmes de userIdA
		// E depois foi realizado os cálculos de similaridade, buscando fazer com que os usuários mais similares fossem pegos
		// Os filmes desses usuários foram pegados, os que A já assistiu foram removidos e depois foram descartados os filmes que apenas alguns usuários
		// semelhantes assistiram, sobrando apenas os mais assistidos por eles
		// Agora é só realizar mais um cálculo da predição
		
		System.out.println("----------------");
		System.out.println("Quantidade de Filmes Possíveis PÓS REDUÇÃO: " + moviesPosReduction.size());
		return moviesPosReduction;
	}

	//***ESTIMAR UMA NOTA QUE O USUÁRIO PODERÁ DAR***
	public Map<Integer, Double> predicao(Map<Integer, Integer> moviesReduction, Map<Integer,List<Rating>> ratingsMini, Map<Integer, Similarity> similarities, List<Rating> ratingsA){
		double mean = 0; // Essa é a média de cada user sem o determinado filme que está sendo analisado, vai mudando durante a iteração
		double ratingX = 0; // Nessa predição é necessário a média e a nota do determinado filme, que nesse caso será o ratingX e ficará mudando durante a iteração
		double somatorioPredicao = 0; // Esse somatório é o numerador da fórmula da predição: (ratingX - mean) * similaridade
		double somatorioSimilaridades = 0; // Esse somatório é o denominador da predição: (abs(similaridade))
		double predicao = 0; // resultado final
		double meanA = 0; // média de A (todos os valores)
		Map<Integer, Double> predicoes = new HashMap<>(); // Key (Id do filme) e Values (Predições da nota desse filme)
		// Esse for é usado apenas para pegar a média de A
		for(Rating rating : ratingsA){
			meanA += rating.getRating();
		}
		meanA = meanA / ratingsA.size();
		
		// 1º for -> Todos os filmes que foram classificados como possíveis filmes para o usuário A
		// 2º for -> para cada filme, todas as similaridades dos usuários serão olhadas
		// 3º for -> As similaridades possuem a lista de cada rating de cada usuário, iremos pegar os ratings de todos os usuários similares
		// If e else if -> usado para determinar a média sem o determiando filme (1º for) e vai incrementando a média com os ratings e caso encontre o filme, será ratingX
		// Mean é a média de todos os filmes desse usuário, com exceção do determinado filme (1º for), logo a média é a soma das notas / (-1 do total)
		// Depois é feito o cálculo do somatorioPredicao e somatorioSimilaridades para este determinado filme desse usuário similar, depois zerado e feito para os outros
		// Finalmente, é feito a predicao da nota para este filme e depois armazenado e feito para os restantes dos filmes
		for(Integer movie : moviesReduction.keySet()){
			for(Similarity s : similarities.values()){
				for(Rating rating : s.getRatingsB()){
					if(rating.getMovieId() != movie){
						mean += rating.getRating();
					}else if(rating.getMovieId() == movie){
						ratingX = rating.getRating();
					}
				}
				mean = mean / (s.getRatingsB().size() - 1);
				somatorioPredicao += (ratingX - mean) * s.getPearsonCorrelation();
				somatorioSimilaridades += Math.abs(s.getPearsonCorrelation());
				mean = 0;
				ratingX = 0;
			}
			predicao = meanA + (somatorioPredicao / somatorioSimilaridades);
			predicoes.put(movie, predicao);
		}
		return predicoes;
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

	public void exportSimilaritiesCSV(String fileName, Collection<Similarity> list){
		try{
			PrintWriter writer = new PrintWriter(fileName+"_similarities", "UTF-8");
			writer.println("userIdA;userIdB;union;intersection;similarity;distanceJ;distanceCos;pearsonCorrelation");
			System.out.println("Exportando Arquivo de Similaridades...");
			for (Similarity s : list) {
				writer.println(s.getUserIdA()+";"+s.getUserIdB()+";"+s.getUnion()+";"+s.getIntersection()+";"+s.getSimilarity()+";"+s.getDistanceJaccard()+";"+s.getDistanceCosseno()+";"+s.getPearsonCorrelation());
			}
			System.out.println("Arquivo de Similaridades Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}
	
	public void exportPredictionsCSV(String fileName, Map<Integer, Double> list){
		try{
			PrintWriter writer = new PrintWriter(fileName+"_predictions", "UTF-8");
			writer.println("movieid;predicao;rating");
			System.out.println("Exportando Arquivo de Predições...");
			for (Integer movie : list.keySet()) {
				writer.println(movie+";"+list.get(movie)+";"+(list.get(movie)*5));
			}
			System.out.println("Arquivo de Predições Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}

}
