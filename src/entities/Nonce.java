package entities;
import java.util.Random;

public class Nonce {

	private Random random;
	private static Nonce INSTANCE;
	
	private Nonce() {
		this.random = new Random();
	}
	
	public static Nonce getInstance() {
		if (INSTANCE == null)
			return new Nonce();
		
		return INSTANCE;
	}
	
	public Long getNonce() {
		return this.random.nextLong();
	}
}
