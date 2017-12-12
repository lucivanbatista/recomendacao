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

	public void iniciarPredicao(int userIdA, String fileName){ // userId que eu quero fazer a predi��o
		//1. Reduzir a consulta
		List<Rating> ratings = preProcessamentoOtimizacao(userIdA);

		//Esse map estar� com todos os ids dos usu�rios da tabela de otimizacao
		Map<Integer, List<Rating>> ratingsAll = this.ratingdao.getQtdUsersReduction();
		ratingsAll =  processamentoOtimizacao(ratings, ratingsAll);

		System.out.println("Quantidade de usu�rios poss�veis similares: " + ratingsAll.size());

		//2. Pegar os ratings de A
		List<Rating> ratingsA = ratingsAll.get(userIdA);
		
		//3. Para cada usu�rio irei analisar a similaridade com o userIdA
		Map<Integer, Similarity> similarities = new HashMap<>(); // Id do usu�rio semelhante ao A e Similarity

		Set<Integer> allUsers = ratingsAll.keySet(); //Todos os usu�rios
		allUsers.remove(userIdA);
		
		//Observa��o: Aqui j� iremos come�ar o processo para predi��o, iremos colocar nesse map os semelhantes de A
		Map<Integer, List<Rating>> ratingsMini = new HashMap<>();
		
		System.out.println("Iniciando c�lculos de similaridade...");
		for(Integer userIdB : allUsers){
			List<Rating> ratingsB = ratingsAll.get(userIdB);

			Similarity s = new Similarity(userIdA, userIdB, ratingsA, ratingsB);

			s = this.recomendacao.recomendarUsingAll(s);

			if(s.getPearsonCorrelation() >= 0.6 || s.getPearsonCorrelation() <= -0.6){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predi��o
			}
		}
		System.out.println("Finalizado os c�lculos de similaridade!");
		
		ratingsAll = null; // Garbage Collection ir� funcionar
		ratings = null;
		System.gc();
		
		// Exportar e excluir tabela de otimiza��o
		exportSimilaritiesCSV(fileName, similarities.values());
		this.ratingdao.deleteTableReduction();
		System.out.println("Quantidade de usu�rios similares: " + similarities.keySet().size());
		
		Map<Integer, Integer> moviesReduction = reductionMovies(ratingsMini, ratingsA); // Filmes que A n�o possui
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
		//At� agora, eu possuo um map com as chaves sendo os usu�rios semelhantes e A, e todos os seus filmes e seus ratings
		Map<Integer, Integer> movies = contarMovies(ratingsMini); // Contar os filmes para c�lculos futuros (remover os menos assistidos)
		
		for(Rating r : ratingsA){ // Removendo os filmes que A j� assistiu
			movies.remove(r.getMovieId());
		}
		
		System.out.println("Quantidade de Filmes Poss�veis PR� REDU��O: " + movies.size());
		
		Map<Integer, Integer> moviesPosReduction = new HashMap<>();
		
		// ESSE FOR POSSIVELMENTE SER� RETIRADO E DEIXAREMOS APENAS OS MOVIES AP�S A CONTAGEM E A ELIMINA��O DOS FILMES QUE A J� ASSISTIU
		for(Integer movieId : movies.keySet()){ // Removendo os filmes que possuem do total de usu�rios similares a metade n�o assistiu
			if(movies.get(movieId) > (ratingsMini.size() / 2)){
				moviesPosReduction.put(movieId, movies.get(movieId));
			}
		}
		movies = null;
		
		// A explica��o � a seguinte, j� que foi realizado um corte dos usu�rios para os que pelo menos assistiram a metade de filmes de userIdA
		// E depois foi realizado os c�lculos de similaridade, buscando fazer com que os usu�rios mais similares fossem pegos
		// Os filmes desses usu�rios foram pegados, os que A j� assistiu foram removidos e depois foram descartados os filmes que apenas alguns usu�rios
		// semelhantes assistiram, sobrando apenas os mais assistidos por eles
		// Agora � s� realizar mais um c�lculo da predi��o
		
		System.out.println("----------------");
		System.out.println("Quantidade de Filmes Poss�veis P�S REDU��O: " + moviesPosReduction.size());
		return moviesPosReduction;
	}

	//***ESTIMAR UMA NOTA QUE O USU�RIO PODER� DAR***
	public Map<Integer, Double> predicao(Map<Integer, Integer> moviesReduction, Map<Integer,List<Rating>> ratingsMini, Map<Integer, Similarity> similarities, List<Rating> ratingsA){
		double mean = 0; // Essa � a m�dia de cada user sem o determinado filme que est� sendo analisado, vai mudando durante a itera��o
		double ratingX = 0; // Nessa predi��o � necess�rio a m�dia e a nota do determinado filme, que nesse caso ser� o ratingX e ficar� mudando durante a itera��o
		double somatorioPredicao = 0; // Esse somat�rio � o numerador da f�rmula da predi��o: (ratingX - mean) * similaridade
		double somatorioSimilaridades = 0; // Esse somat�rio � o denominador da predi��o: (abs(similaridade))
		double predicao = 0; // resultado final
		double meanA = 0; // m�dia de A (todos os valores)
		Map<Integer, Double> predicoes = new HashMap<>(); // Key (Id do filme) e Values (Predi��es da nota desse filme)
		// Esse for � usado apenas para pegar a m�dia de A
		for(Rating rating : ratingsA){
			meanA += rating.getRating();
		}
		meanA = meanA / ratingsA.size();
		
		// 1� for -> Todos os filmes que foram classificados como poss�veis filmes para o usu�rio A
		// 2� for -> para cada filme, todas as similaridades dos usu�rios ser�o olhadas
		// 3� for -> As similaridades possuem a lista de cada rating de cada usu�rio, iremos pegar os ratings de todos os usu�rios similares
		// If e else if -> usado para determinar a m�dia sem o determiando filme (1� for) e vai incrementando a m�dia com os ratings e caso encontre o filme, ser� ratingX
		// Mean � a m�dia de todos os filmes desse usu�rio, com exce��o do determinado filme (1� for), logo a m�dia � a soma das notas / (-1 do total)
		// Depois � feito o c�lculo do somatorioPredicao e somatorioSimilaridades para este determinado filme desse usu�rio similar, depois zerado e feito para os outros
		// Finalmente, � feito a predicao da nota para este filme e depois armazenado e feito para os restantes dos filmes
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
		Map<Integer, Integer> movies = new HashMap<>(); // Lista com os filmes dos usu�rios semelhantes a A
		
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
		System.out.println("Organizando o banco... criando tabela para otimiza��o");
		this.ratingdao.createTableReduction();
		System.out.println("Inserindo elementos na tabela para otimiza��o...");
		this.ratingdao.insertTableReduction(userId);
		System.out.println("Recebendo ratings...");
		List<Rating> ratings = this.ratingdao.selectTableReduction(userId);
		return ratings;
	}

	public Map<Integer,List<Rating>> processamentoOtimizacao(List<Rating> ratings, Map<Integer,List<Rating>> ratingsAll){
		// Resumo: pegarei todos os ratings e irei colocar para cada rating em seu devido id (distribui��o dos ratings nas listas dos userId dentro desse map)
		System.out.println("Organizando ratings...");
		for(Rating r : ratings){
			List<Rating> temp = ratingsAll.get(r.getUserId());
			temp.add(r);
			ratingsAll.put(r.getUserId(), temp);
		}
		
		System.out.println("Ratings dos usu�rios organizados!");

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
			System.out.println("Exportando Arquivo de Predi��es...");
			for (Integer movie : list.keySet()) {
				writer.println(movie+";"+list.get(movie)+";"+(list.get(movie)*5));
			}
			System.out.println("Arquivo de Predi��es Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}

}
