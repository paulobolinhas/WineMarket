package Entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class AuthenticationValidator {

	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	private FileInputStream trustStoreFile;
	private KeyStore trustStore;
	private PublicKey clientPublicKey;
	
	public AuthenticationValidator(ObjectInputStream inStream, ObjectOutputStream outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}
	
	public void sendNonce() throws IOException {
		Nonce generatedNonce = Nonce.getInstance();
		this.outStream.writeObject((Long) generatedNonce.getNonce());
		System.out.println("Enviado Nonce");
	}
	
	public Long receiveNonce() throws ClassNotFoundException, IOException {
		return (Long) inStream.readObject();
	}
	
	public byte[] receiveSignature() throws ClassNotFoundException, IOException {
		return (byte[]) inStream.readObject();
	}
	
	public void loadTrustStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		this.trustStoreFile = new FileInputStream("src//keys//truststore");
		this.trustStore = KeyStore.getInstance("JKS");
		trustStore.load(trustStoreFile, "truststore".toCharArray());
	}
	
	public Certificate getCertificate(String clientID) throws KeyStoreException {
		return this.trustStore.getCertificate("client"+clientID+"Keys");
	}
	
	public PublicKey getPublicKey(Certificate certificate) {
		return this.clientPublicKey = certificate.getPublicKey();
	}
	
	public void loadCertificateAndPublicKey(String clientID) throws KeyStoreException {
		Certificate c = this.trustStore.getCertificate("client"+clientID+"Keys");
		this.clientPublicKey = c.getPublicKey();
	}
	
	public Signature getSignature() throws NoSuchAlgorithmException {
		return Signature.getInstance("SHA256withRSA");
	}
	
	public boolean verifySignature(Long nonceFromClient, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature s = this.getSignature();
		s.initVerify(this.clientPublicKey);
		s.update(nonceFromClient.toString().getBytes()); // isto deve estar mal

		return s.verify(signature);
	}
}
