package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import catalogs.MessageCatalog;
import catalogs.SellsCatalog;
import catalogs.UserCatalog;
import catalogs.WineCatalog;
import domain.entities.AuthenticationValidator;
import domain.entities.BlockChain;
import domain.entities.FileEncryptorDecryptor;
import domain.entities.IntegrationChecker;
import domain.entities.Mensagem;
import domain.entities.Sell;
import domain.entities.Transaction;
import domain.entities.User;
import domain.entities.Wine;
import domain.handlers.ReceiveImagesHandler;
import domain.handlers.SendImagesHandler;
import enums.TransactionType;

public class TintolmarketServer {

	public static final String USERSCATFILE = "./src/userCatalog.txt";
	public static final String USERSCATFILENCRYPTED = "./src/userCatalogEncrypted.txt";
	public static final String WINECATFILE = "./src/wineCatalog.txt";
	public static final String SELLSCATFILE = "./src/sellsCatalog.txt";
	public static final String MSGCATFILE = "./src/messageCatalog.txt";
	public static final String WALLETFILE = "./src/userWallet.txt";

	public static UserCatalog userCatalog;
	public static SellsCatalog sellsCatalog;
	public static WineCatalog wineCatalog;
	private static MessageCatalog messageCatalog;
	private static BlockChain blockchain;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		System.out.println("servidor: main");

		TintolmarketServer tintolServer = new TintolmarketServer();

		// TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>
		int port = 12345;
		String passwordCifra = args[1];
		String serverKeyAlias = args[2];
		String passwordServerKeyStore = args[3];

		if (args.length == 4) {
			port = Integer.parseInt(args[0]);
		}

		userCatalog = UserCatalog.getInstance(USERSCATFILE, WALLETFILE, passwordCifra);
		sellsCatalog = SellsCatalog.getSellsCatalog();
		wineCatalog = WineCatalog.getWineCatalog();
		messageCatalog = MessageCatalog.getMessageCatalog();

		try {
			KeyStore serverKeys = KeyStore.getInstance("JKS");
			serverKeys.load(new FileInputStream("./src/keys/" + serverKeyAlias), passwordServerKeyStore.toCharArray());
			PrivateKey pk = (PrivateKey) serverKeys.getKey(serverKeyAlias, passwordServerKeyStore.toCharArray());
			blockchain = BlockChain.getInstance(pk);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ------------------------------------
		// CIFRA

		File usersCatFile = new File(USERSCATFILE);

		if (usersCatFile.exists()) {
			FileEncryptorDecryptor.encryptUsersCat(USERSCATFILE, passwordCifra);
		}

		// ------------------------------------
		// INTEGRIDADE

		File integridadeVerificadaWine = new File(WINECATFILE);
		File integridadeVerificadaSell = new File(SELLSCATFILE);
		File integridadeVerificadaMsg = new File(MSGCATFILE);
		File integridadeVerificadaWallet = new File(WALLETFILE);

		if (IntegrationChecker.checkSumIntegrityVerification(integridadeVerificadaWine, WINECATFILE)
				&& IntegrationChecker.checkSumIntegrityVerification(integridadeVerificadaSell, SELLSCATFILE)
				&& IntegrationChecker.checkSumIntegrityVerification(integridadeVerificadaMsg, MSGCATFILE)
				&& IntegrationChecker.checkSumIntegrityVerification(integridadeVerificadaWallet, WALLETFILE)) {
			System.out.println("Os ficheiros estao integros.");
		} else {
			System.exit(0);
		}

		System.setProperty("javax.net.ssl.keyStore", "src//keys//" + serverKeyAlias);
		System.setProperty("javax.net.ssl.keyStorePassword", passwordServerKeyStore);

		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket sslServerSocket = (SSLServerSocket) ssf.createServerSocket(port);

		tintolServer.startServer(sslServerSocket);
	}

	@SuppressWarnings("resource")
	public void startServer(SSLServerSocket sslServerSocket) throws IOException, NoSuchAlgorithmException {

		initializeMemory();

		// uma thread por ligacao
		while (true) {

			try {
				new ClientHandler(sslServerSocket.accept()).start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	protected void initializeMemory() throws IOException {

		userCatalog.initializeUserCatalog();
		initializeSellsCatalog();
		initializeWineCatalog();
		initializeMessagesStore();
		
//		try {
			blockchain.initializeBlockChain();
//			if (!blockchain.verify(userCatalog)) {
//				System.out.println("Blockchain corrompida");
//				System.exit(0);
//			}
//		} catch (NoSuchAlgorithmException | CertificateException | IOException | InvalidKeyException | SignatureException e) {
//			e.printStackTrace();
//		}
	}

	private synchronized void initializeMessagesStore() {
		File messagesFile = new File(MSGCATFILE);

		Scanner fileSc = null;
		try {
			fileSc = new Scanner(messagesFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		while (fileSc.hasNextLine()) {
			String[] currentLine = fileSc.nextLine().split(";");
			if (!currentLine[0].equals(""))
				messageCatalog.add(new Mensagem(currentLine[0], currentLine[1], currentLine[2]));
		}

		fileSc.close();
	}

	private synchronized void initializeSellsCatalog() {
		File sellsFile = new File(SELLSCATFILE);

		Scanner sc = null;
		try {
			sc = new Scanner(sellsFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		while (sc.hasNextLine()) {
			String[] currentLine = sc.nextLine().split(";");
			sellsCatalog.add(new Sell(currentLine[0], currentLine[1], Integer.parseInt(currentLine[2]),
					Integer.parseInt(currentLine[3]), currentLine[4]));
		}

		sc.close();
	}

	private synchronized void initializeWineCatalog() {

		File wineFile = new File(WINECATFILE);

		Scanner sc = null;
		try {
			sc = new Scanner(wineFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		while (sc.hasNextLine()) {
			String[] currentLine = sc.nextLine().split(";");
			wineCatalog.add(new Wine(currentLine[0], currentLine[1], Integer.parseInt(currentLine[2]),
					Integer.parseInt(currentLine[3])));
		}

		sc.close();

	}

	class ClientHandler extends Thread {

		private Socket socket = null;
		private ObjectOutputStream outStream;
		private ObjectInputStream inStream;
		private AuthenticationValidator authValidator;
		
		ClientHandler(Socket sslServerSocket) {
			socket = sslServerSocket;

			try {
				outStream = new ObjectOutputStream(socket.getOutputStream());
				inStream = new ObjectInputStream(socket.getInputStream());
				authValidator = new AuthenticationValidator(inStream, outStream);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Erro nas streams da socket");
			}
		}

		public void run() {
			try {

				String clientID = null;
				boolean clientExistsFlag = false;

				try {
					clientID = (String) inStream.readObject();
					System.out.println("Cliente com ID " + clientID + " quer autenticar-se");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				String certificadoStr = "client" + clientID + "KeyRSApub.cer";
				clientExistsFlag = userCatalog.exists(clientID);

				// indicar o cliente se ele existe ou nao
				outStream.writeObject(clientExistsFlag);

				authValidator.sendNonce();

				try {

					Long nonceFromClient = authValidator.receiveNonce();

					byte[] signedContent = authValidator.receiveSignature();

					Certificate certificate = null;

					if (clientExistsFlag) {
						String certificateStr = userCatalog.getCertificadoByID(clientID);
						certificate = authValidator.getCertificate(certificateStr);
					} else {
						certificate = authValidator.getCertificate();
					}

					authValidator.loadPublicKey(certificate);

					boolean isClientValid = authValidator.verifySignature(nonceFromClient, signedContent);
					outStream.writeObject(isClientValid);

					if (!isClientValid) {
						System.out.println("Cliente Corrompido! A fechar o servidor");
						exitFunc(inStream, outStream, socket);
					}

					if (!clientExistsFlag) {

						userCatalog.registNewUser(clientID, certificadoStr);
						userCatalog.registNewWallet(clientID);
						userCatalog.add(new User(clientID, certificadoStr));

						outStream.writeObject("Novo cliente " + clientID + " registado");
					}

				} catch (ClassNotFoundException | KeyStoreException | NoSuchAlgorithmException | InvalidKeyException
						| SignatureException | CertificateException e) {
					System.out.println("ERRO - Um problema ocorreu com a validacao da autenticacao.");
					e.printStackTrace();
				}

				interactWUser(clientID);

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void interactWUser(String clientID) {

			String menu = getMenu();
			String userAction = "";

			try {
				while (!userAction.equals("exit")) {

					outStream.writeObject(menu);

					try {
						userAction = (String) inStream.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					String[] userActionSplited = userAction.split(" ");
					;
					int arraySize = userActionSplited.length;

					if (userActionSplited[0].equals("add") || userActionSplited[0].equals("a") && arraySize == 3) {
						outStream.writeObject(addFunc(userActionSplited[1], userActionSplited[2]));

					} else if (userActionSplited[0].equals("sell")
							|| userActionSplited[0].equals("s") && arraySize == 4) {
						outStream.writeObject(sellFunc(userActionSplited[1], Integer.parseInt(userActionSplited[2]),
								Integer.parseInt(userActionSplited[3]), clientID));

					} else if (userActionSplited[0].equals("view")
							|| userActionSplited[0].equals("v") && arraySize == 2) {
						viewFunc(userActionSplited[1]);

					} else if (userActionSplited[0].equals("buy")
							|| userActionSplited[0].equals("b") && arraySize == 4) {
						outStream.writeObject(buyFunc(userActionSplited[1], Integer.parseInt(userActionSplited[3]),
								userActionSplited[2], clientID));

					} else if (userActionSplited[0].equals("wallet") || userActionSplited[0].equals("w")) {
						outStream.writeObject("Saldo: " + walletFunc(clientID));

					} else if ((userActionSplited[0].equals("classify") || userActionSplited[0].equals("c"))
							&& arraySize == 3) {
						outStream.writeObject(
								classifyFunc(userActionSplited[1], Integer.parseInt(userActionSplited[2])));

					} else if ((userActionSplited[0].equals("talk") || userActionSplited[0].equals("t"))) {

						String message = "";
						for (int i = 2; i < userActionSplited.length; i++) {
							message += userActionSplited[i] + " ";
						}

						outStream.writeObject(
								talkFunc(USERSCATFILE, MSGCATFILE, clientID, message, userActionSplited[1]));

					} else if (userActionSplited[0].equals("read") || userActionSplited[0].equals("r")) {
						outStream.writeObject(readFunc(MSGCATFILE, clientID));

					} else if (userActionSplited[0].equals("exit") || userActionSplited[0].equals("e")) {
						outStream.writeObject("Disconnected");
						System.out.println("Client Disconnected");
						break;

					} else if (userActionSplited[0].equals("list") || userActionSplited[0].equals("l")) {
						outStream.writeObject("\n" + blockchain.toString());
					} else {
						outStream.writeObject("Invalid action.\n");
						continue;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private String addFunc(String wineID, String image) throws IOException {

			ReceiveImagesHandler rcvImgHandler = new ReceiveImagesHandler(inStream, "./src/imgServer/");

			if (wineCatalog.exists(wineID)) {
				rcvImgHandler.consumeInput(); // assumindo que a imagem existe
				return "This wine already exists.";
			}

			try {
				rcvImgHandler.receiveImage(image);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String wineRegist = "";
			if (wineCatalog.getSize() == 0)
				wineRegist = (wineID + ";" + image + ";0;0");
			else
				wineRegist = ("\n" + wineID + ";" + image + ";0;0");

			OutputStream addWine = new FileOutputStream(WINECATFILE, true);

			synchronized (addWine) {
				addWine.write(wineRegist.getBytes(), 0, wineRegist.length());
			}

			wineCatalog.add(new Wine(wineID, image, 0, 0));

			addWine.close();

			return "Wine added.";
		}

		private String sellFunc(String wineID, int value, int quantity, String seller) throws IOException {

			if (!wineCatalog.exists(wineID))
				return "This wine doesnt exist";

			Wine wine = wineCatalog.getWineByID(wineID);
			Sell currentSell = sellsCatalog.getSale(wineID, seller);

			Boolean alreadyOnSale = currentSell != null;

			if (alreadyOnSale) {
				String sellCheck = currentSell.toString();
				if (value == currentSell.getValue()) {
					editFile(SELLSCATFILE, sellCheck, value, quantity, "sell");
					return "Wine is now on sale.";
				} else {
					editFile(SELLSCATFILE, sellCheck, value, quantity, "sellDifPrice");
					return "Wine is now on sale.";
				}
			}

			try {

				Transaction currentTransaction = blockchain.createTransaction(TransactionType.SELL, wineID, quantity,
						value, seller);
				
				outStream.writeObject("Do you want to confirm the SELL operation? (yes or no)");

				String confirmation = (String) inStream.readObject(); // Receber confirmacao

				if (confirmation.equals("yes"))
					outStream.writeObject(currentTransaction.getDataToSign());
				else
					return "SELL operation canceled";
				// adicionar o que acontece se nao

				String dataToVerify = (String) inStream.readObject();
				byte[] signedContent = (byte[]) inStream.readObject();

				//verificar assinatura
				boolean isSignatureValid = authValidator.verifySignature(dataToVerify, signedContent);
				
				if (!isSignatureValid)
					return "Content signed incorrect, SELL operation canceled";
				
				currentTransaction.setSignature(signedContent);
				blockchain.addTransaction(currentTransaction);

			} catch (ClassNotFoundException | IOException | InvalidKeyException | NoSuchAlgorithmException
					| SignatureException e) {
				e.printStackTrace();
			}

			String wineRegist = "";

			if (sellsCatalog.getSize() == 0)
				wineRegist = (wineID + ";" + wine.getImage() + ";" + value + ";" + quantity + ";" + seller);
			else
				wineRegist = ("\n" + wineID + ";" + wine.getImage() + ";" + value + ";" + quantity + ";" + seller);

			OutputStream addWineSell = new FileOutputStream(SELLSCATFILE, true);
			synchronized (addWineSell) {
				addWineSell.write(wineRegist.getBytes(), 0, wineRegist.length());
				addWineSell.close();
			}

			sellsCatalog.add(new Sell(wineID, wine.getImage(), value, quantity, seller));

			return "Wine is now on sale.";
		}

		private void viewFunc(String wine) throws IOException {

			StringBuilder result = new StringBuilder();
			if (wineCatalog.exists(wine)) {

				Wine currentWine = wineCatalog.getWineByID(wine);

				result.append(wine + " information:\n Image: " + currentWine.getImage() + "\n Average Classification: "
						+ currentWine.getAvgClassification() + "\n");

				ArrayList<Sell> wineSales = sellsCatalog.getSalesByWineID(wine);

				if (!wineSales.isEmpty() && existsSaleDifFromZeroQuantity(wineSales)) {
					result.append("Wine Seller(s): \n");
					for (Sell sell : wineSales) {
						if (sell.getQuantity() > 0) {
							result.append(" Seller: " + sell.getSeller() + "; Value: " + sell.getValue()
									+ "; Quantity: " + sell.getQuantity() + "\n");
						}
					}
				}

				outStream.writeObject(result.toString());
				outStream.writeObject(currentWine.getImage());

				SendImagesHandler sendImgHandler = new SendImagesHandler(outStream, "./src/imgServer/");

				if (!sendImgHandler.sendImage(currentWine.getImage())) {
					System.out.println("This file doesnt exists");
				}

			} else
				outStream.writeObject("This Wine doesnt exist");
		}

		private boolean existsSaleDifFromZeroQuantity(ArrayList<Sell> wineSales) {

			for (Sell sell : wineSales) {
				if (sell.getQuantity() != 0)
					return true;
			}

			return false;
		}

		private String buyFunc(String wineID, int quantityToBuy, String sellerID, String clientID) throws IOException {

			String errorCase = "\nReasons why you can't buy this wine:\n\n"
					+ " - This wine does not exists or it isn't available on this seller's stock;\n"
					+ " - Wine's seller and client are the same;\n"
					+ " - Quantity not available or insufficient funds.";

			Sell currentSale = sellsCatalog.getSale(wineID, sellerID);
			if (currentSale == null || clientID.equals(sellerID) || quantityToBuy <= 0)
				return errorCase;

			int winePrice = currentSale.getValue();
			Boolean isPurchasable = currentSale.getQuantity() >= quantityToBuy
					&& winePrice * quantityToBuy <= walletFunc(clientID);
			if (!isPurchasable)
				return errorCase;

			try {

				Transaction currentTransaction = blockchain.createTransaction(TransactionType.BUY, wineID,
						quantityToBuy, winePrice, clientID);
				System.out.println("Enviar confirmacao de VENDA ao cliente para obter a assinatura");
				outStream.writeObject("Do you want to confirm the BUY operation? (yes or no)");

				String confirmation = (String) inStream.readObject(); // Receber confirmacao

				if (confirmation.equals("yes"))
					outStream.writeObject(currentTransaction.getDataToSign());
				else
					return "BUY operation canceled";
				// adicionar o que acontece se nao

				String dataToVerify = (String) inStream.readObject();
				byte[] signedContent = (byte[]) inStream.readObject();
				System.out.println("DATA SIGNED IN BYTES " + signedContent);
				currentTransaction.setSignature(signedContent);

				//verificar assinatura
				boolean isSignatureValid = authValidator.verifySignature(dataToVerify, signedContent);
				
				if (!isSignatureValid)
					return "Content signed incorrect, BUY operation canceled";

				blockchain.addTransaction(currentTransaction);

			} catch (ClassNotFoundException | IOException | InvalidKeyException | NoSuchAlgorithmException
					| SignatureException e) {
				e.printStackTrace();
			}

			int clientNewBalance = walletFunc(clientID) - quantityToBuy * winePrice;
			int sellerNewBalance = walletFunc(sellerID) + quantityToBuy * winePrice;

			String clientBalance = new StringBuilder().append(clientID + ";" + String.valueOf(clientNewBalance))
					.toString();
			String sellerBalance = new StringBuilder().append(sellerID + ";" + String.valueOf(sellerNewBalance))
					.toString();

			// client's wallet
			editWalletFile(WALLETFILE, clientBalance, clientNewBalance, "client");

			// seller's wallet
			editWalletFile(WALLETFILE, sellerBalance, sellerNewBalance, "seller");

			editFile(SELLSCATFILE, currentSale.toString(), winePrice, quantityToBuy, "buy");
			return "Wine purchased.";

		}

		private int walletFunc(String clientID) {
			return userCatalog.getUserByID(clientID).getBalance();
		}

		private synchronized String classifyFunc(String wine, int stars) {

			if (stars < 1 || stars > 5)
				return "Your classification must be from 1 to 5";

			if (wineCatalog.exists(wine)) {

				// memory
				Wine currentWine = wineCatalog.getWineByID(wine);
				currentWine.updateClassification(stars);

				// file
				File winesCatalog = new File(WINECATFILE);

				Scanner winesSc = null;

				try {
					winesSc = new Scanner(winesCatalog);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				Boolean isFound = false;
				while (winesSc.hasNextLine() && !isFound) {

					String wineFileLine = winesSc.nextLine();
					String[] wineFileLineSplitted = wineFileLine.split(";");

					if (wineFileLineSplitted[0].equals(wine)) {

						File fileToBeModified = new File(WINECATFILE);
						BufferedReader reader = null;
						FileWriter writer = null;

						isFound = true;
						String oldContent = "";

						try {
							reader = new BufferedReader(new FileReader(fileToBeModified));

							String line = reader.readLine();
							// Reading all the lines of input text file into oldContent
							while (line != null) {
								oldContent = oldContent + line + System.lineSeparator();
								line = reader.readLine();
							}

							String newContentWithoutNewLine = "";

							// Replacing oldString with newString in the oldContent
							String newString = wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";"
									+ (String.valueOf(Integer.parseInt(wineFileLineSplitted[2]) + stars)) + ";"
									+ String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) + 1);
							String newContent = oldContent.replace(wineFileLine, newString);
							newContentWithoutNewLine = newContent.substring(0, newContent.length() - 2);

							// Rewriting the input text file with newContent
							writer = new FileWriter(fileToBeModified);
							writer.write(newContentWithoutNewLine);

						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								reader.close();
								writer.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}

				return "Classification atributed";
			} else
				return "This Wine doesnt exist.";

		}

		private String talkFunc(String usersFilename, String messagesFilename, String clientIDSender, String message,
				String clientIDDest) throws IOException {

			if (userCatalog.exists(clientIDDest)) {

				OutputStream addMessage = new FileOutputStream(messagesFilename, true);

				String messageRegist;
				if (messageCatalog.getSize() == 0)
					messageRegist = (clientIDSender + ";" + clientIDDest + ";" + message);
				else
					messageRegist = ("\n" + clientIDSender + ";" + clientIDDest + ";" + message);

				messageCatalog.add(new Mensagem(clientIDSender, clientIDDest, message));
				synchronized (addMessage) {
					addMessage.write(messageRegist.getBytes(), 0, messageRegist.length());
					addMessage.close();
				}

				return "Message sent.";
			}

			return "Unsent message, user not found.";
		}

		private synchronized String readFunc(String filename, String clientID) {

			StringBuilder sb = new StringBuilder();

			if (messageCatalog.existsMessagesFor(clientID)) {
				ArrayList<Mensagem> messagesForClient = messageCatalog.getMessagesForClient(clientID);

				for (Mensagem m : messagesForClient) {
					sb.append(m.getSender() + ":" + m.getMessage() + "\n");
					messageCatalog.remove(m);
				}

				File messagesFile = new File(filename);

				Scanner messagesSC = null;

				try {
					messagesSC = new Scanner(messagesFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}

				while (messagesSC.hasNextLine()) {

					String messageFileLine = messagesSC.nextLine();
					String[] messageFileLineSplitted = messageFileLine.split(";");

					if (clientID.equals(messageFileLineSplitted[1])) {
						editFile(filename, messageFileLine, 0, 0, "read");
					}
				}

				return sb.toString();

			} else
				return "You dont have any message to read.";

		}

		private void exitFunc(ObjectInputStream inStream, ObjectOutputStream outStream, Socket sock)
				throws IOException {
			inStream.close();
			outStream.close();
			sock.close();
			System.exit(0);
		}

		private synchronized void editFile(String filename, String editFileLine, int value, int quantity,
				String operation) {

			File winesCatalog = new File(filename);
			Scanner winesSc = null;

			try {
				winesSc = new Scanner(winesCatalog);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			while (winesSc.hasNextLine() && !isFound) {

				String wineFileLine = winesSc.nextLine();
				String[] wineFileLineSplitted = wineFileLine.split(";");

				if (editFileLine.equals(wineFileLine)) {
					isFound = true;

					File fileToBeModified = new File(filename);
					String oldContent = "";
					BufferedReader reader = null;
					FileWriter writer = null;

					try {
						reader = new BufferedReader(new FileReader(fileToBeModified));

						// Reading all the lines of input text file into oldContent

						String line = reader.readLine();

						while (line != null) {

							oldContent = oldContent + line + System.lineSeparator();
							line = reader.readLine();
						}

						String newContentWithoutNewLine = "";

						switch (operation) {

						case "buy":

							String newStringBuy = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";" + value
									+ ";" + String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) - quantity) + ";"
									+ wineFileLineSplitted[4]);

							String newContentBuy = oldContent.replace(wineFileLine, newStringBuy);
							newContentWithoutNewLine = newContentBuy.substring(0, newContentBuy.length() - 2);
							sellsCatalog.getSale(wineFileLineSplitted[0], wineFileLineSplitted[4])
									.setQuantity(Integer.parseInt(wineFileLineSplitted[3]) - quantity);
							break;

						case "sell":

							String newStringSell = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1] + ";"
									+ value + ";" + String.valueOf(Integer.parseInt(wineFileLineSplitted[3]) + quantity)
									+ ";" + wineFileLineSplitted[4]);

							String newContentSell = oldContent.replace(wineFileLine, newStringSell);
							newContentWithoutNewLine = newContentSell.substring(0, newContentSell.length() - 2);
							sellsCatalog.getSale(wineFileLineSplitted[0], wineFileLineSplitted[4])
									.setQuantity(Integer.parseInt(wineFileLineSplitted[3]) + quantity);
							sellsCatalog.getSale(wineFileLineSplitted[0], wineFileLineSplitted[4]).setValue(value);
							break;

						case "sellDifPrice":

							String newStringSellDifPrice = (wineFileLineSplitted[0] + ";" + wineFileLineSplitted[1]
									+ ";" + value + ";" + quantity + ";" + wineFileLineSplitted[4]);

							String newContentSellDifPrice = oldContent.replace(wineFileLine, newStringSellDifPrice);
							newContentWithoutNewLine = newContentSellDifPrice.substring(0,
									newContentSellDifPrice.length() - 2);

							sellsCatalog.getSale(wineFileLineSplitted[0], wineFileLineSplitted[4])
									.setQuantity(quantity);
							sellsCatalog.getSale(wineFileLineSplitted[0], wineFileLineSplitted[4]).setValue(value);
							break;

						case "read":
							String newStringRead = "";
							String newContentRead = oldContent.replace(wineFileLine + "\r\n", newStringRead);

							if (newContentRead.equals(""))
								newContentWithoutNewLine = "";
							else
								newContentWithoutNewLine = newContentRead.substring(0, newContentRead.length() - 2);

							break;
						}

						writer = new FileWriter(fileToBeModified);
						writer.write(newContentWithoutNewLine);

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							reader.close();
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();

						}
					}
				}
			}
		}

		private synchronized void editWalletFile(String userWallet, String editFileline, int balance, String ID) {

			File usersWallet = new File(userWallet);
			Scanner walletSc = null;

			try {
				walletSc = new Scanner(usersWallet);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Boolean isFound = false;
			while (walletSc.hasNextLine() && !isFound) {

				String walletFileLine = walletSc.nextLine();
				String[] walletFileLineSplitted = walletFileLine.split(":");

				if (editFileline.split(";")[0].equals(walletFileLineSplitted[0])) {
					isFound = true;

					File fileToBeModified = new File(userWallet);

					String oldContent = "";

					BufferedReader reader = null;

					FileWriter writer = null;

					try {
						reader = new BufferedReader(new FileReader(fileToBeModified));

						String line = reader.readLine();

						while (line != null) {

							oldContent = oldContent + line + System.lineSeparator();
							line = reader.readLine();
						}

						String newContentWithoutNewLine = "";

						switch (ID) {
						case "client":
							String newStringCWallet = (walletFileLineSplitted[0] + ":" + String.valueOf(balance));
							String newContentCWallet = oldContent.replace(walletFileLine, newStringCWallet);
							newContentWithoutNewLine = newContentCWallet.substring(0, newContentCWallet.length() - 2);
							userCatalog.getUserByID(walletFileLineSplitted[0]).setBalance(balance);
							break;

						case "seller":
							String newStringSWallet = (walletFileLineSplitted[0] + ":" + String.valueOf(balance));
							String newContentSWallet = oldContent.replace(walletFileLine, newStringSWallet);
							newContentWithoutNewLine = newContentSWallet.substring(0, newContentSWallet.length() - 2);
							userCatalog.getUserByID(walletFileLineSplitted[0]).setBalance(balance);
							break;
						}

						writer = new FileWriter(fileToBeModified);

						writer.write(newContentWithoutNewLine);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							reader.close();

							writer.close();
						} catch (IOException e) {
							e.printStackTrace();

						}
					}
				}

			}
		}

		private String getMenu() {
			return "\nActions:\nadd <wine> <image>\n" + "sell <wine> <value> <quantity>\n" + "view <wine>\n"
					+ "buy <wine> <seller> <quantity>\n" + "wallet\n" + "classify <wine> <stars>\n"
					+ "talk <user> <message>\n" + "read\n" + "list\n" + "exit\n";
		}

	}

}
