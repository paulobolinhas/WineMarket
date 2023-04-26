package domain.handlers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientAuthenticationHandler {

	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
	private static ClientAuthenticationHandler INSTANCE;
	
	public static ClientAuthenticationHandler getInstance(ObjectOutputStream outStream, ObjectInputStream inStream) {
		if (INSTANCE == null)
			INSTANCE = new ClientAuthenticationHandler(outStream, inStream);
	
		return INSTANCE;
	}
	
	private ClientAuthenticationHandler(ObjectOutputStream outStream, ObjectInputStream inStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}
	
}
