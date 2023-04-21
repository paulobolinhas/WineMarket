package domain;

public class Mensagem {

	private String sender;
	private String recipient;
	private String message;

	public Mensagem(String sender, String recipient, String message) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getMessage() {
		return message;
	}

}