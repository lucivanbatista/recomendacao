package logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.MovieDAO;
import dao.RatingDAO;
import model.Movie;
import model.Rating;
import model.Similarity;

public class Prediction {

	private Recomendar recomendacao;
	private RatingDAO ratingdao;
	private MovieDAO moviedao;
	private String fileName;
	private int divisor;

	public Prediction(String fileName) {
		recomendacao = new Recomendar();
		ratingdao = new RatingDAO();
		moviedao = new MovieDAO();
		this.fileName = fileName;
		this.divisor = 2;
	}

	public void iniciarPredicao(int userIdA){ // userId que eu quero fazer a predi��o
		//1. Reduzir a consulta
		List<Rating> ratings = preProcessamentoOtimizacao(userIdA);

		//Esse map estar� com todos os ids dos usu�rios da tabela de otimizacao
		Map<Integer, List<Rating>> ratingsAll = this.ratingdao.getQtdUsersReduction();
		ratingsAll =  processamentoOtimizacao(ratings, ratingsAll);
		
		ratings = null;
		System.gc();
		
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

			if(s.getPearsonCorrelation() >= 0.7 || s.getPearsonCorrelation() <= -0.7){
				similarities.put(userIdB, s); // Calcular as similaridades
				ratingsMini.put(userIdB, ratingsB); // Usado para predi��o
			}
		}
		System.out.println("Finalizado os c�lculos de similaridade!");
		
		ratingsAll = null; // Garbage Collection ir� funcionar
		System.gc();
		
		this.ratingdao.deleteTableReduction();
		System.out.println("Quantidade de usu�rios similares: " + similarities.keySet().size());
		
		// ideia fazer, tipo um loop, caso ele n�o tenha usu�rio similares, ent�o eu fa�o novamente o mesmo processo e coloco o c�digo abaixo em uma fun��o
		// colocar para pegar o /3 e depois /4 l� no ratingdao isso deopis da dele��o das tabelas
		if(similarities.keySet().size() < 5){
			ratingsMini = null;
			ratingsA = null;
			similarities = null;
			allUsers = null;
			ratings = null;
			System.gc();
			this.divisor++;
			System.out.println("N�o foi poss�vel encontrar usu�rios similares o suficiente, iremos aumentar a procura...");
			iniciarPredicao(userIdA);
		}else{
			finalPredicao(ratingsMini, ratingsA, similarities);
		}		
	}
	
	public void finalPredicao(Map<Integer, List<Rating>> ratingsMini, List<Rating> ratingsA, Map<Integer, Similarity> similarities){
		Map<Integer, Integer> moviesReduction = reductionMovies(ratingsMini, ratingsA); // Filmes que A n�o possui; (Id movies, Qtd)
		
		System.out.println("Iniciando Predi��o 1");
		List<Rating> predicoes1 = predicao1(moviesReduction, similarities, ratingsA);
		
		System.out.println("Iniciando Predi��o 2");
		List<Rating> predicoes2 = predicao2(moviesReduction, similarities, ratingsA);
		
		exportSimilaritiesCSV(this.fileName, similarities.values());
		exportPredictionsCSV(this.fileName+"_1", predicoes1);
		exportPredictionsCSV(this.fileName+"_2", predicoes2);
		exportNamesTitleCSV(this.fileName, ratingsA);
	}
	
	public Map<Integer, Integer> reductionMovies(Map<Integer,List<Rating>> ratingsMini, List<Rating> ratingsA){ // RECOMENDAR FILMES
		//At� agora, eu possuo um map com as chaves sendo os usu�rios semelhantes e A, e todos os seus filmes e seus ratings
		Map<Integer, Integer> movies = contarMovies(ratingsMini); // Contar os filmes para c�lculos futuros (remover os menos assistidos)
		
		for(Rating r : ratingsA){ // Removendo os filmes que A j� assistiu
			movies.remove(r.getMovieId());
		}
		
//		System.out.println("Quantidade de Filmes Poss�veis PR� REDU��O: " + movies.size());
//		Map<Integer, Integer> moviesPosReduction = new HashMap<>();
//		
//		// ESSE FOR POSSIVELMENTE SER� RETIRADO E DEIXAREMOS APENAS OS MOVIES AP�S A CONTAGEM E A ELIMINA��O DOS FILMES QUE A J� ASSISTIU
//		for(Integer movieId : movies.keySet()){ // Removendo os filmes que possuem do total de usu�rios similares a metade n�o assistiu
//			if(movies.get(movieId) > (ratingsMini.size() / 2)){
//				moviesPosReduction.put(movieId, movies.get(movieId));
//			}
//		}
//		movies = null;
		
		// A explica��o � a seguinte, j� que foi realizado um corte dos usu�rios para os que pelo menos assistiram a metade de filmes de userIdA
		// E depois foi realizado os c�lculos de similaridade, buscando fazer com que os usu�rios mais similares fossem pegos
		// Os filmes desses usu�rios foram pegados, os que A j� assistiu foram removidos e depois foram descartados os filmes que apenas alguns usu�rios
		// semelhantes assistiram, sobrando apenas os mais assistidos por eles
		// Agora � s� realizar mais um c�lculo da predi��o
		
		System.out.println("----------------");
		System.out.println("Quantidade de Filmes Poss�veis P�S REDU��O: " + movies.size());
		return movies;
	}

	//***ESTIMAR UMA NOTA QUE O USU�RIO PODER� DAR***
	public List<Rating> predicao2(Map<Integer, Integer> moviesReduction, Map<Integer, Similarity> similarities, List<Rating> ratingsA){
		double mean = 0; // Essa � a m�dia de cada user sem o determinado filme que est� sendo analisado, vai mudando durante a itera��o
		double ratingX = 0; // Nessa predi��o � necess�rio a m�dia e a nota do determinado filme, que nesse caso ser� o ratingX e ficar� mudando durante a itera��o
		double somatorioPredicao = 0; // Esse somat�rio � o numerador da f�rmula da predi��o: (ratingX - mean) * similaridade
		double somatorioSimilaridades = 0; // Esse somat�rio � o denominador da predi��o: (abs(similaridade))
		double predicao = 0; // resultado final
		double meanA = 0; // m�dia de A (todos os valores)
		List<Rating> predicoes = new ArrayList<>(); // Key (Id do filme) e Values (Predi��es da nota desse filme)
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
			predicoes.add(new Rating(movie,predicao));
			somatorioPredicao = 0;
			somatorioSimilaridades = 0;
			predicao = 0;
		}
		return predicoes;
	}
	
	public List<Rating> predicao1(Map<Integer, Integer> moviesReduction, Map<Integer, Similarity> similarities, List<Rating> ratingsA){
		// Para cada usu�rio, vou pegar o somat�rio das notas dos itens que os usu�rios deram para ele e dividir pelo modulo da similaridade do userA com ele
		// Ou seja, pego todos os itens, depois para o usu�rio B, pego a nota e divido pela similaridade e no final desse item mostro a predicao dele
		// Isso � tipo uma m�dia
		double somatorioPredicao = 0; // Esse � o somat�rio das notas que B deu para um determinado item
		int cont = 0;
		double predicao = 0; // resultado final
		List<Rating> predicoes = new ArrayList<>();
		
		for(Integer movie : moviesReduction.keySet()){ // Todos os filmes classificados como poss�veis filmes de A
			for(Similarity s : similarities.values()){ // para cada similaridade (usu�rio) vou pegar o determinado filme e sua nota
				for(Rating rating : s.getRatingsB()){ // irei colocar no somatorio e no final dividir por qtd similares para pegar a predicao
					if(rating.getMovieId() == movie){ // esse � a nota do determinado filme
						somatorioPredicao += rating.getRating();
						cont++; // para pegar o qtd similares que possuem esse item
					}					
				}
			}
			predicao = somatorioPredicao / cont;
			predicoes.add(new Rating(movie, predicao));
			cont = 0;
			somatorioPredicao = 0;
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
		this.ratingdao.insertTableReduction(userId, this.divisor);
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
			writer.println("userIdA;userIdB;intersection;similarity;distanceJ;distanceCos;pearsonCorrelation");
			System.out.println("Exportando Arquivo de Similaridades...");
			for (Similarity s : list) {
				writer.println(s.getUserIdA()+";"+s.getUserIdB()+";"+s.getIntersection()+";"+s.getSimilarity()+";"+s.getDistanceJaccard()+";"+s.getDistanceCosseno()+";"+s.getPearsonCorrelation());
			}
			System.out.println("Arquivo de Similaridades Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}
	
	public void exportPredictionsCSV(String fileName, List<Rating> list){
		Map<Integer, Movie> movies = moviedao.selectAllMovies();
		try{
			PrintWriter writer = new PrintWriter(fileName+"_predictions", "UTF-8");
			writer.println("movieid;predicao;rating;movieTitle");
			System.out.println("Exportando Arquivo de Predi��es...");
			Collections.sort(list);
			for (Rating r : list) {
				writer.println(r.getMovieId()+";"+r.getRating()+";"+(r.getRating()*5)+";"+movies.get(r.getMovieId()).getTitle());
			}
//			for (Integer movie : list.keySet()) {
//				writer.println(movie+";"+list.get(movie)+";"+(list.get(movie)*5+";"+movies.get(movie).getTitle()));
//			}
			System.out.println("Arquivo de Predi��es Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}
	
	public void exportNamesTitleCSV(String fileName, List<Rating> list){
		Map<Integer, Movie> movies = moviedao.selectAllMovies();
		try{
			PrintWriter writer = new PrintWriter(fileName+"_movies", "UTF-8");
			writer.println("movieid;movieTitle");
			System.out.println("Exportando Arquivo de Filmes de A...");
			for (Rating r : list) {
				writer.println(r.getMovieId()+";"+movies.get(r.getMovieId()).getTitle());
			}
			System.out.println("Arquivo de Filmes de A Exportado com sucesso!");

			writer.close();
		}catch (Exception e) {
			System.out.println("[ERROR]: "+e.toString());
		}
	}

}
