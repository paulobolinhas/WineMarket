package entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class ClientAuthentication {

	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private KeyStore clientKeyStore;
	
	public ClientAuthentication(ObjectInputStream inStream, ObjectOutputStream outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}
	
	public Long receiveNonce() throws ClassNotFoundException, IOException {
		return (Long) inStream.readObject();
	}
	
	public PrivateKey loadTSAndPK(String keyStoreAlias, String passKeyStoreString) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		this.clientKeyStore = KeyStore.getInstance("JKS");
		this.clientKeyStore.load(new FileInputStream("src//keys//"+keyStoreAlias), passKeyStoreString.toCharArray());
		return (PrivateKey)this.clientKeyStore.getKey(keyStoreAlias, passKeyStoreString.toCharArray());
	}
	
	public void SendSignature(Long nonceFromServer, PrivateKey privateKey) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException {
		Signature signature = Signature.getInstance("SHA256withRSA");;
		signature.initSign(privateKey);
		signature.update(nonceFromServer.toString().getBytes());

		this.outStream.writeObject(nonceFromServer);
		this.outStream.writeObject(signature.sign());
	}
	
}
