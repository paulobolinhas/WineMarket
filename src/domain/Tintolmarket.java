package domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import domain.entities.ClientAuthentication;
import domain.entities.Mensagem;
import domain.handlers.ReceiveImagesHandler;
import domain.handlers.SendImagesHandler;

public class Tintolmarket {

	// private ClientAuthenticationHandler clientAuthHandler;

	public static void main(String[] args) {

		// Tintolmarket <serverAddress> <truststore> <keystore> <password-keystore>
		// <userID>
		String hostname = "";
		int port = 12345;

		String[] ipport = args[0].split(":");
		hostname = ipport[0];
		if (ipport.length == 2)
			port = Integer.parseInt(ipport[1]);

		String trustStoreAlias = args[1];
		String keyStoreAlias = args[2];
		String passKeyStoreString = args[3];
		String userID = args[4];

		System.setProperty("javax.net.ssl.trustStore", "src//keys//" + trustStoreAlias);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStoreAlias);
		SocketFactory sf = SSLSocketFactory.getDefault();

		try (SSLSocket sslSocket = (SSLSocket) sf.createSocket(hostname, port)) {

			ObjectOutputStream outStream = new ObjectOutputStream(sslSocket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(sslSocket.getInputStream());

			// clientAuthHandler = ClientAuthenticationHandler.getInstance(outStream,
			// inStream);

			Scanner clientInterface = new Scanner(System.in);

			outStream.writeObject(userID); // userID = pedido de autenticacao

			boolean clientExistsFlag = false;

			try {
				clientExistsFlag = (boolean) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			ClientAuthentication clientAuth = new ClientAuthentication(inStream, outStream);
			Long nonceFromServer = null;

			PrivateKey privateKey = null;

			try {

				nonceFromServer = clientAuth.receiveNonce();
				System.out.println("nonce recebido do servidor: " + nonceFromServer);

				privateKey = clientAuth.loadKSAndPK(keyStoreAlias, passKeyStoreString);
				clientAuth.SendSignature(nonceFromServer, privateKey);

				if (!clientExistsFlag) {
					Certificate certificate = clientAuth.getCertificate(keyStoreAlias);

					clientAuth.sendCertificate(certificate);
				}

			} catch (ClassNotFoundException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException
					| CertificateException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}

			boolean isClientValid = false;
			try {
				isClientValid = (boolean) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (!isClientValid) {
				System.out.println("Cliente Corrompido! A fechar o cliente");
				clientInterface.close();
				inStream.close();
				outStream.close();
				sslSocket.close();
				System.exit(0);
			}

			if (!clientExistsFlag)
				try {
					System.out.println((String) inStream.readObject());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			try {

				String userAction = "";

				while (!userAction.equals("exit")) {

					System.out.println((String) inStream.readObject()); // menu
					System.out.println("Choose action:\n");

					userAction = clientInterface.nextLine();
					String[] userActionSplited = userAction.split(" "); //se a msg tiver mais do que uma palavra funciona?

					if (userActionSplited[0].equals("talk") || userActionSplited[0].equals("t")) {

						String toEncrypt = "";
						for (int i = 2; i < userActionSplited.length; i++) {
							toEncrypt += userActionSplited[i] + " ";
						}

						KeyStore trustStore = KeyStore.getInstance("JKS");
						trustStore.load(new FileInputStream("./src/keys/" + trustStoreAlias),
								trustStoreAlias.toCharArray());

						String path = "src/certificates/" + "client" + userActionSplited[1] + "KeyRSApub.cer";
						FileInputStream is = new FileInputStream(path);
						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						Certificate c = cf.generateCertificate(is);
						is.close();

						PublicKey pk = c.getPublicKey();

						Cipher cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.ENCRYPT_MODE, pk);

						// Encrypt the toEncrypt string
						byte[] encryptedData = cipher.doFinal(toEncrypt.getBytes());

						userAction = userActionSplited[0] + " " + userActionSplited[1];
						outStream.writeObject(userAction);
						outStream.writeObject(Base64.getEncoder().encodeToString(encryptedData));
					} else {

						outStream.writeObject(userAction);
					}
					if (userActionSplited[0].equals("add") || userActionSplited[0].equals("a")) {
						SendImagesHandler sendImgHandler = new SendImagesHandler(outStream, "./src/imgClient/");
						try {
							sendImgHandler.sendImage(userActionSplited[2]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (userActionSplited[0].equals("sell") || userActionSplited[0].equals("s")
							|| userActionSplited[0].equals("buy") || userActionSplited[0].equals("b")) {

						// verificar se é a mensagem de confirmação ou erro no buy
						String serverResponse = (String) inStream.readObject();

						if (serverResponse.contains("Reasons why you can't buy this wine:")) {
							System.out.println(serverResponse);
							continue;
						} else {

							System.out.println(serverResponse); // mensagem de confirmacao
							outStream.writeObject(clientInterface.nextLine()); // sim ou nao

							String dataToSign = (String) inStream.readObject();

							clientAuth.SendSignature(dataToSign, privateKey); // envia data e data assinado
						}
					}

					String result = (String) inStream.readObject();
					
					System.out.println("RESULT " + result);
					if (userActionSplited[0].equals("read") || userActionSplited[0].equals("r")) {
						
						Scanner sc = new Scanner(result);
						
						Cipher cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.DECRYPT_MODE, clientAuth.getPrivateKey());
						StringBuilder sb = new StringBuilder();
						sb.append("Mensagens recebidas: \n");
						
						while(sc.hasNextLine()) {
							String currentLine = sc.nextLine();
							String[] currentLineSplitted = currentLine.split(":");
							System.out.println("CURRENT LINE " + currentLine);
							String msgEncrypted = currentLineSplitted[1];
							String msgDecrypted = decryptMessage(msgEncrypted, cipher);
							sb.append(" Remetente: " + currentLineSplitted[0] + ";\n Mensagem: " + msgDecrypted + " \n\n");
						}	
						
						result = sb.toString();
						sc.close();
					}
					
					System.out.println(result);

					if ((userActionSplited[0].equals("view") || userActionSplited[0].equals("v"))
							&& !result.equals("This Wine doesnt exist")) {

						String imgName = (String) inStream.readObject();
						ReceiveImagesHandler rcvImgHandler = new ReceiveImagesHandler(inStream, "./src/imgClient/");

						try {
							rcvImgHandler.receiveImage(imgName);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				clientInterface.close();
				inStream.close();
				outStream.close();
				sslSocket.close();
				System.exit(0);

			} catch (ClassNotFoundException | InvalidKeyException | SignatureException | NoSuchAlgorithmException
					| KeyStoreException | CertificateException | IllegalBlockSizeException | BadPaddingException
					| NoSuchPaddingException e) {
				e.printStackTrace();
			}

		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}
	
	private static String decryptMessage(String msgEncrypted, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
		byte[] decryptedData = cipher.doFinal(msgEncrypted.getBytes());
		return new String(decryptedData);
	}

	private static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

	private static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getLong();
	}

}
