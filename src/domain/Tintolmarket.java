package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Tintolmarket {

	public static void main(String[] args) {

		// Tintolmarket <serverAddress> <truststore> <keystore> <password-keystore>
		// <userID>
		String hostname = "";
		int port = 12345;

		String[] ipport = args[0].split(":");
		hostname = ipport[0];
		if (ipport.length == 2)
			port = Integer.parseInt(ipport[1]);

		String trustStore = args[1];
		String keyStore = args[2];
		String passwordKeystore = args[3];
		String userID = args[4];

		System.setProperty("javax.net.ssl.trustStore", "src//keys//" + trustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStore);
		SocketFactory sf = SSLSocketFactory.getDefault();

		try (SSLSocket sslSocket = (SSLSocket) sf.createSocket(hostname, port)) {

			ObjectOutputStream outStream = new ObjectOutputStream(sslSocket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(sslSocket.getInputStream());
			Scanner clientInterface = new Scanner(System.in);

			outStream.writeObject(args[1]); // userID = pedido de autenticacao

			//receber o nonce. verificar a resposta de quando o utilizador é desconhecido
			Long nonce = null;
			try {
				nonce = (Long) inStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println("nonce: " + nonce);
			
			//Nao sei se a password cifra é a password de autenticacao, mas este codigo verificava se a pass era passada como argumento,
			//alterado temporariamente para receber na linha de comandos
//			if (args.length == 2) {
			System.out.println("Password:");
			String password = clientInterface.nextLine();
			outStream.writeObject(password);
//			} else {
//				outStream.writeObject(args[2]); // password
//			}

			try {

				String loginCheck = (String) inStream.readObject();

				if (loginCheck.equals("erroPass")) {
					System.out.println("Password invalida. Programa Terminado.");
					System.exit(0);
				} else if (loginCheck.equals("NovoRegisto")) {
					System.out.println("Novo cliente registado.");
				}

				String userAction = "";

				while (!userAction.equals("exit")) {

					System.out.println((String) inStream.readObject()); // menu
					System.out.println("Choose action:\n");

					userAction = clientInterface.nextLine();

					outStream.writeObject(userAction);

					String[] userActionSplited = userAction.split(" ");

					if (userActionSplited[0].equals("add") || userActionSplited[0].equals("a")) {

						SendImagesHandler sendImgHandler = new SendImagesHandler(outStream, "./src/imgClient/");

						try {
							sendImgHandler.sendImage(userActionSplited[2]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					String result = (String) inStream.readObject();
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

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (UnknownHostException ex) {

			System.out.println("Server not found: " + ex.getMessage());

		} catch (IOException ex) {

			System.out.println("I/O error: " + ex.getMessage());
		}
	}

}
