package domain;
import java.util.Random;

public class Nonce {

	private Random random;
	
	public Nonce() {
		this.random = new Random();
	}
	
	public Long generatedNewNonce() {
		return this.random.nextLong();
	}
}
