package catalogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import domain.User;

public class UserCatalog {

	private String usersStr;
	private String userWalletsStr;
	
	private static UserCatalog INSTANCE;
	private ArrayList<User> userCatalog;

	private UserCatalog(String usersStr, String userWalletsStr) {
		userCatalog = new ArrayList<User>();
		this.usersStr = usersStr;
		this.userWalletsStr = userWalletsStr;
	}

	public static UserCatalog getInstance(String usersStr, String userWalletsStr) {
		if (INSTANCE == null)
			INSTANCE = new UserCatalog(usersStr, userWalletsStr);
		
		return INSTANCE;
	}

	public synchronized User getUserByID(String id) {

		for (User u : userCatalog) 
			if (u.getID().equals(id))
				return u;

		return null;

	}

	public synchronized int watchWallet(User u) {
		return u.getBalance();
	}

	public synchronized void add(User user) {
		userCatalog.add(user);
	}

	public synchronized Boolean exists(String clientID) {
		return INSTANCE.getUserByID(clientID) != null;
	}

	public synchronized int getSize() {
		return userCatalog.size();
	}
	
	public synchronized String getCertificadoByID(String ID) {
		for (User u: this.userCatalog)
			if(u.getID().equals(ID))
				return u.getCertificado();
		return null;
	}
	
	public void registNewUser(String clientID, String certificadoStr) throws IOException {
		File usersCatalog = new File(this.usersStr);
		
		String newClient = "\n" + clientID + ":" + certificadoStr;
		
		if (this.getSize() == 0)
			newClient = clientID + ":" + certificadoStr;
		
		OutputStream clientRegister = new FileOutputStream(usersCatalog, true);
		synchronized (clientRegister) {
			clientRegister.write(newClient.getBytes(), 0, newClient.length());
			clientRegister.close();
		}
	}
	
	public void registNewWallet(String clientID) throws IOException {
		File userWallets = new File(this.userWalletsStr);
		
		String usersBalance = "\n" + clientID + ":200";
		if (this.getSize() == 0)
			usersBalance = clientID + ":200";

		OutputStream wallet = new FileOutputStream(userWallets, true);
		synchronized (wallet) {
			wallet.write(usersBalance.getBytes(), 0, usersBalance.length());
			wallet.close();
		}
	}
	
	public static void decryptUsers(String inputFile, String outputFile, String password)
	        throws GeneralSecurityException, IOException {

	    FileInputStream fis = new FileInputStream(inputFile);
	    FileOutputStream fos = new FileOutputStream(outputFile);

	    byte[] salt = new byte[8];
	    fis.read(salt);

	    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); 
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

	    KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
	    SecretKey key = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");

	    byte[] iv = new byte[16];
	    fis.read(iv);

	    AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
	    params.init(new IvParameterSpec(iv));
	    cipher.init(Cipher.DECRYPT_MODE, key, params);

	    // Decifra o usersCatalog
	    byte[] in = new byte[64]; 
	    int read;
	    while ((read = fis.read(in)) != -1) {
	        byte[] output = cipher.update(in, 0, read);
	        if (output != null) {
	            fos.write(output);
	        }
	    }
	    byte[] output = cipher.doFinal();
	    if (output != null) {
	        fos.write(output);
	    }

	    fis.close();
	    fos.flush();
	    fos.close();
	}
	
	public void initializeUserCatalog() {
		File usersFile = new File(this.usersStr);
		
		Scanner fileSc = null;
		try {
			fileSc = new Scanner(usersFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		while (fileSc.hasNextLine()) {
			String[] currentLine = fileSc.nextLine().split(":");
			this.add(new User(currentLine[0], currentLine[1]));
		}

		fileSc.close();
		
		File userWallets = new File(this.userWalletsStr);

		Scanner walletSc = null;
		try {
			walletSc = new Scanner(userWallets);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		while (walletSc.hasNextLine()) {
			String[] currentLine = walletSc.nextLine().split(":");
			this.getUserByID(currentLine[0]).setBalance(Integer.parseInt(currentLine[1]));
		}

		walletSc.close();
	}


}