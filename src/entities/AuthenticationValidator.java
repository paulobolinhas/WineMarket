package entities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.security.cert.CertificateFactory;

public class AuthenticationValidator {

	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	private KeyStore trustStore;
	private PublicKey clientPublicKey;
	
	public AuthenticationValidator(ObjectInputStream inStream, ObjectOutputStream outStream) {
		this.inStream = inStream;
		this.outStream = outStream;
	}
	
	public void sendNonce() throws IOException {
		Nonce generatedNonce = Nonce.getInstance();
		this.outStream.writeObject((Long) generatedNonce.getNonce());
		System.out.println("Servidor->Cliente");
	}
	
	public Long receiveNonce() throws ClassNotFoundException, IOException {
		return (Long) inStream.readObject();
	}
	
	public byte[] receiveSignature() throws ClassNotFoundException, IOException {
		return (byte[]) inStream.readObject();
	}
	
	public Certificate getCertificate(String certificateName) throws KeyStoreException, FileNotFoundException, CertificateException {
		FileInputStream is = new FileInputStream("src//keys//"+certificateName);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return cf.generateCertificate(is);
	}
	
	public PublicKey getPublicKey(Certificate certificate) {
		return this.clientPublicKey = certificate.getPublicKey();
	}
	
	public void loadPublicKey(Certificate c) throws KeyStoreException {
		this.clientPublicKey = c.getPublicKey();
	}
	
	public Signature getSignature() throws NoSuchAlgorithmException {
		return Signature.getInstance("SHA256withRSA");
	}
	
	public boolean verifySignature(Long nonceFromClient, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature s = this.getSignature();
		s.initVerify(this.clientPublicKey);
		s.update(nonceFromClient.toString().getBytes());
		return s.verify(signature);
	}
}
