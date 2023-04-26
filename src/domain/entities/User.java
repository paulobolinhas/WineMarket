package domain.entities;

public class User {

	private String id;
	private String certificado;
	private int balance;

	public User(String id, String certificado) {
		this.id = id;
		this.certificado = certificado;
		this.setBalance(200);
	}
	
	public String getID() {
		return this.id;
	}

	public String getCertificado() {
		return this.certificado;
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	protected void setNewPass(String newPass) {
		this.certificado = newPass;
	}

}