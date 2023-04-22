package catalogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

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

}