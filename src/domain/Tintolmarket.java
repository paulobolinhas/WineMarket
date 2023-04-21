package domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Tintolmarket {

	public static void main(String[] args) {

		String hostname = "";
		int port = 12345;
		
		String[] ipport = args[0].split(":");
		hostname = ipport[0];
		if (ipport.length == 2)
			port = Integer.parseInt(ipport[1]);
		
		try (Socket socket = new Socket(hostname, port)) {

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			Scanner clientInterface = new Scanner(System.in);
			
			outStream.writeObject(args[1]); //userID
			
			if (args.length == 2) {
				System.out.println("Password:");
				String password = clientInterface.nextLine();
				outStream.writeObject(password);
			} else {
				outStream.writeObject(args[2]); //password
			}

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
					
					if ((userActionSplited[0].equals("view") || userActionSplited[0].equals("v")) && !result.equals("This Wine doesnt exist")) {
						
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
				socket.close();
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
