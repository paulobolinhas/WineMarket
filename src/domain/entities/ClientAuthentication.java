package domain.entities;

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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class ClientAuthentication {

	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private KeyStore clientKeyStore;
	private PrivateKey clientPrivateKey;
	
	public ClientAuthentication(ObjectInputStream inStream, ObjectOutputStream outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}
	
	public Long receiveNonce() throws ClassNotFoundException, IOException {
		return (Long) inStream.readObject();
	}
	
	public PrivateKey loadKSAndPK(String keyStoreAlias, String passKeyStoreString) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		this.clientKeyStore = KeyStore.getInstance("JKS");
		this.clientKeyStore.load(new FileInputStream("src//keys//"+keyStoreAlias), passKeyStoreString.toCharArray());
		return this.clientPrivateKey = (PrivateKey)this.clientKeyStore.getKey(keyStoreAlias, passKeyStoreString.toCharArray());
	}
	
	public PrivateKey getPrivateKey() {
		return this.clientPrivateKey;
	}
	
	public void SendSignature(Long dataToSign, PrivateKey privateKey) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(dataToSign.toString().getBytes());

		this.outStream.writeObject(dataToSign);
		this.outStream.writeObject(signature.sign());
	}
	
	public void SendSignature(String dataToSign, PrivateKey privateKey) throws InvalidKeyException, SignatureException, IOException, NoSuchAlgorithmException {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(dataToSign.getBytes());

		this.outStream.writeObject(dataToSign);
		this.outStream.writeObject(signature.sign());
	}
	
	public Certificate getCertificate(String keyStoreAlias) throws KeyStoreException {
		return this.clientKeyStore.getCertificate(keyStoreAlias);
	}

	public void sendCertificate(Certificate certificate) throws IOException {
		this.outStream.writeObject(certificate); //será que passar o objecto funciona ou tenho de o passar para bytes?
	}
	
}
