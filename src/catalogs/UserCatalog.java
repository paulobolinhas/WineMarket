package catalogs;

import java.util.ArrayList;

import domain.User;

public class UserCatalog {

	private static UserCatalog instance;
	private ArrayList<User> userCatalog;

	private UserCatalog() {
		userCatalog = new ArrayList<User>();
	}

	public static UserCatalog getUserCatalog() {
		if (instance == null)
			instance = new UserCatalog();
		
		return instance;
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
		return instance.getUserByID(clientID) != null;
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

}