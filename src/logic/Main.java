package logic;

public class Main {

	public static void main(String[] args) {
		Prediction p = new Prediction("user1");
		p.iniciarPredicao(1);
		p = null;
		System.gc();
		
		Prediction q = new Prediction("lucivan");
		q.iniciarPredicao(32004983);
		q = null;
		System.gc();

		Prediction r = new Prediction("max");
		r.iniciarPredicao(72968123);
		
	}

}
