package domain;

public class User {

	private String id;
	private String password;
	private int balance;

	public User(String id, String password) {
		this.id = id;
		this.password = password;
		this.setBalance(200);
	}

	public String getID() {
		return this.id;
	}

	private String showPassword() {
		return this.password;
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	protected void setNewPass(String newPass) {
		this.password = newPass;
	}

	public boolean isPasswordCorrect(String password) {
		return this.showPassword().equals(password);
	}
}